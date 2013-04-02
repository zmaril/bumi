(ns bumi.core
  (:require [bumi.titan :refer (start)]
            [bumi.git :refer (RevCommit->map rev-list)]
            [clojurewerkz.titanium.graph :as g]
            [clojurewerkz.titanium.vertices :as v]
            [clojurewerkz.titanium.edges :as e]
            [clojurewerkz.titanium.types :as t]
            [ogre.core :as q])
  (:import (org.apache.commons.io FileUtils)))

(defn project-commit [{:keys [hash 
                              author
                              committer
                              message
                              mentions
                              changed-files
                              parents] :as commit}]
  (g/transact!
   (let [commit-node   (v/unique-upsert! :hash {:hash hash 
                                                :message message
                                                :type "commit"})
         author-node    (v/unique-upsert! :name (select-keys author [:name :email :type]))
         committer-node (v/unique-upsert! :name (select-keys committer [:name :email :type]))
         author-edge-info    (select-keys author    [:date :timezone])
         committer-edge-info (select-keys committer [:date :timezone])]
     (e/upconnect! author-node    :authored  commit-node author-edge-info)
     (e/upconnect! committer-node :committed commit-node committer-edge-info)
     (doseq [[mention-type people] mentions]
       (doseq [person people]
         (e/upconnect! commit-node mention-type (v/unique-upsert! :name person))))
     (doseq [[filename action] changed-files]
       (let [file-node (v/unique-upsert! :filename {:filename filename
                                                    :type "file"})
             action-label        (condp = action
                                   :edit :editted
                                   :new  :created)]
         (e/upconnect! commit-node action-label file-node)))
     (doseq [parent-hash parents]
         (e/upconnect! commit-node :child-of (v/unique-upsert! :hash {:hash parent-hash
                                                                      :type "commit"}))))))

(defn clear-db []
  (FileUtils/deleteDirectory (java.io.File. "/tmp/cassandra")))

(defn -main [& args]
  (clear-db)
  (start)
  (doseq [rev rev-list]
    (-> rev
        RevCommit->map
        project-commit
        ))
  nil)

;; (g/transact! 
;;  (q/query (v/find-by-id (:__id__ (first vs))) 
;;           q/<E--
;;           (q/transform e/to-map)
;;           q/into-vec!))

;; (g/transact! 
;;  (v/to-map (first (v/find-by-kv :name "Linus Torvalds")))

;; )