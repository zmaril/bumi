(ns bumi.core
  (:require [bumi.pallet :refer (init-server kill-server)]
            [bumi.titan :refer (start)]
            [bumi.git :refer (RevCommit->map rev-list)]
            [clojurewerkz.titanium.graph :as g]
            [clojurewerkz.titanium.vertices :as v]
            [clojurewerkz.titanium.edges :as e]
            [clojurewerkz.titanium.types :as t]
            [ogre.core :as q])
  (:import (org.apache.commons.io FileUtils)))

(def commit-count (atom 0))
(def person-count (atom 0))
(def file-count (atom 0))
(def projection-count (atom 0))

(defn clear-db []
  (FileUtils/deleteDirectory (java.io.File. "/tmp/cassandra")))

;;Not sure if this should be in Titanium yet
(defn unique-find-by-kv [key value]  
  (let [results (v/find-by-kv key value)]
    (when (< 2 (count results))
      (throw (Throwable. "Expected no more than one result.")))
    (first results)))

(defn project-commit [{:as commit
                       :keys [hash author committer message
                              mentions changed-files parents]}]
  (println (swap! projection-count inc) hash)
  (g/retry-transact! 
   3 1000
   (let [commit-node    (unique-find-by-kv :hash hash)
         author-node    (unique-find-by-kv :name (:name author))
         committer-node (unique-find-by-kv :name (:name committer))
         author-edge-info    (select-keys author    [:date :timezone])
         committer-edge-info (select-keys committer [:date :timezone])]
     ;;Connect people to commit
     (e/connect! author-node    :authored  commit-node author-edge-info)
     (e/connect! committer-node :committed commit-node committer-edge-info)
     (doseq [[mention-type people] mentions]
       (doseq [person people]
         (e/connect! commit-node mention-type (unique-find-by-kv :name (:name person)))))

     ;;Connect commit to changed files
     (doseq [[filename action] changed-files]
       (let [file-node (unique-find-by-kv :filename filename)
             action-label        (condp = action
                                   :edit    :edited
                                   :add     :added
                                   :delete  :deleted)]
         (e/connect! commit-node action-label file-node)))

     ;;Connect commits to parents
     (doseq [parent-hash parents]
       (v/upsert! :hash {:hash parent-hash})
       (e/connect! commit-node :child-of (unique-find-by-kv :hash parent-hash))))))

(defn create-person [person]
  (println (swap! file-count inc) (:name person))
  (g/transact! (v/create! person)))

(defn create-file [filename]
  (println (swap! file-count inc) filename)
  (g/transact! (v/create! {:filename filename :type "file"})))

(defn create-commit [commit]
  (println (swap! commit-count inc) (:hash commit))
  (g/transact! (v/create! commit)))

(defn load-db []
  (clear-db)
  (start)
  (let [rev-maps   (pmap RevCommit->map rev-list)
        filenames              (->> rev-maps 
                                    (map (comp (partial map first) :changed-files))
                                    flatten
                                    set)
        authors-and-committers (->> rev-maps
                                    (map (juxt :author :committer))
                                    flatten
                                    (map #(select-keys % [:name :type])))
        mentioned-people       (->> rev-maps                                    
                                    (map (comp vals :mentions))                                    
                                    flatten)
        people (set (concat mentioned-people authors-and-committers))
        commits (map #(select-keys % [:hash :type :message]) rev-maps)]    
    (println "Mapping over names.")
    (dorun (pmap create-person people))
    (println "All names loaded.")
    (println "Mapping over files.")
    (dorun (pmap create-file  filenames))
    (println "All files loaded.")
    (println "Mapping over commits.")
    (dorun (pmap create-commit commits))
    (println "All commits loaded.")
    (dorun (pmap project-commit rev-maps))
    (println "Everything connected. All done!")))


(defn -main [& args]
  (println args)
  (case (first args)
    "init-server" (init-server)
    "kill-server" (kill-server)
    "load" (load-db)
;;    "analyze" (analyze-db)
    )
  (println "All done!")
  (System/exit 0))