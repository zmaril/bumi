(ns bumi.core
  (:use     [bumi.config :only (debug-println)])
  (:require [bumi.git :as git]
            [bumi.titan :as titan]
            [hermes.core :as g]
            [hermes.vertex :as v]
            [hermes.edge :as e]
            [clojure.data.xml :as xml]))

;;Various numbers used to print out status information when BUMI_DEBUG
;;is true
(def tag-index (atom 0))
(def commit-index (atom 0))
(def total-tags (atom 0))
(def total-commits (atom 0))

;;TODO: this should be moved into hermes sometime soon. It's useful.
;;Probably needs error checking though
(defn unique-upsert! [& args]
  (let [upserted (apply v/upsert! args)]
    (if (= 1 (count upserted))
      (first upserted)
      (do (println args upserted)
          (throw (Throwable.
                  "Don't call unique-upsert! when there is more than one element returned."))))))

(defn connect-commit-to-diff [commit-node diff]
  (let [diff-node (unique-upsert! :filename {:filename (:filename diff)
                                             :type "file"})]
    (e/upconnect! commit-node diff-node "changed" (dissoc diff :filename :diff))))

(defn connect-commit-to-parent [commit-node parent-hash]
  (let [parent-node (unique-upsert! :hash {:hash parent-hash
                                           :type "commit"})]
    (e/upconnect! commit-node parent-node "child-of")))

(defn connect-commit-to-people-mentioned
  [commit-node [key people]]
  (doseq [person people]
    (let [person-node (unique-upsert! :name (dissoc person :date :timezone))]
      (e/upconnect! commit-node person-node (name key)))))

(defn time-between-retries
  [i] (* i i 100))

(defn load-commit-into-titan [{:keys [people-mentioned diffs message
                                      hash parents author committer] :as commit}]
  (debug-println "INFO: Hash being loaded is " hash " "
                 (swap! commit-index inc) "/" @total-commits) 
  (try (g/retry-transact!
        4 time-between-retries
        (let [author-node    (unique-upsert! :name (-> author
                                                       (assoc :type "person")
                                                       (dissoc :date :timezone)))
              committer-node (unique-upsert! :name (-> committer
                                                       (assoc :type "person")
                                                       (dissoc :date :timezone)))
              commit-node    (unique-upsert! :hash {:type "commit"
                                                    :hash hash
;;                                                    :message message
                                                    })]
          (e/upconnect! author-node commit-node "authored" {:date (:date author)})
          (e/upconnect! committer-node commit-node "committed" {:date (:date committer)})
          (doseq [diff diffs]
            (connect-commit-to-diff commit-node diff))
          (doseq [parent parents]
            (connect-commit-to-parent commit-node parent))
          (doseq [type-of-mention people-mentioned]
            (connect-commit-to-people-mentioned commit-node type-of-mention))))
       (catch Exception e
         (debug-println "ERROR: Issues with loading " hash e))))

(defn load-tag-into-titan [{:keys [tagger object tag-name date] :as tag}]
  (debug-println "INFO: Tag being loaded is " tag-name
                 (swap! tag-index inc) "/" @total-tags)
  (try
    (g/retry-transact!
     4 time-between-retries
     (let [object-node    (unique-upsert! :hash object)
           tagger-node    (when (and (:name tagger) (:email tagger))
                            (unique-upsert! :name tagger))
           tag-node       (unique-upsert! :tag-name {:type "tag"
                                                     :tag-name tag-name})]
       (when tagger-node
         (e/upconnect! tagger-node tag-node "authored" {:date date}))
       (e/upconnect! tag-node object-node "tagged")))
    (catch Exception e
      (debug-println "ERROR: Issues with loading " tag-name e (.getMessage e)))))

(defn upload-repo []
  (debug-println "INFO: Starting upload.")
  (titan/start)
  (let [tags (git/git-tag-list)
        commits (git/git-rev-list)]
    (swap! total-tags (constantly (count tags)))
    (swap! total-commits (constantly (count commits)))
    (doall (map (comp load-tag-into-titan git/parse-tag-from-name) tags))
    (doall (map (comp load-commit-into-titan git/parse-commit-from-hash) commits)))    
  (debug-println "INFO: Upload successful. WAHOOOOOOO!")
  (System/exit 0))

(defn -main [& args]
  (upload-repo))