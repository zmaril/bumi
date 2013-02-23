(ns bumi.core
  (:use     [bumi.config :only (debug-println storage-dir)])
  (:require [bumi.git :as git]
            [bumi.titan :as titan]
            [hermes.core :as g]
            [hermes.vertex :as v]
            [hermes.edge :as e]
            [clojure.data.xml :as xml]))

(def tag-index (atom 0))
(def commit-index (atom 0))
(def total-tags (atom 0))
(def total-commits (atom 0))

;;All of the connect-commit-to[x] happen inside of a transact! anyway
(defn connect-commit-to-diff [commit-node diff]
  (let [diff-node (first (v/upsert! :filename {:filename (:filename diff)
                                               :type "file"}))]
    (e/upconnect! commit-node diff-node "changed" (dissoc diff :filename))))

(defn connect-commit-to-parent [commit-node parent-hash]
  (let [parent-node (first (v/upsert! :commit-hash {:commit-hash parent-hash}))]
    (e/upconnect! commit-node parent-node "child-of")))

(defn connect-commit-to-people-mentioned
  [commit-node [key people]]
  (doseq [person people]
    (let [person-node (first (v/upsert! :name (dissoc person :date :timezone)))]
      (e/upconnect! commit-node person-node (name key)))))

(defn load-commit-into-titan [{:keys [people-mentioned diffs message
                                      commit-hash parents author committer] :as commit}]
  (debug-println "INFO: Hash being loaded is " commit-hash " "
                 (swap! commit-index inc) "/" @total-commits)
  (try (g/retry-transact!
        4 (fn [i] (* i i 100))
        (let [author-node    (first (v/upsert! :name (assoc author    :type "person")))
              committer-node (first (v/upsert! :name (assoc committer :type "person")))
              commit-node    (first (v/upsert! :commit-hash {:type "commit"
                                                             :commit-hash commit-hash
                                                             :message message}))]
          (e/upconnect! author-node commit-node "authored")
          (e/upconnect! committer-node commit-node "committed")
          (doseq [diff diffs]
            (connect-commit-to-diff commit-node diff))
          (doseq [parent parents]
            (connect-commit-to-parent commit-node parent))
          (doseq [type-of-mention people-mentioned]
            (connect-commit-to-people-mentioned commit-node type-of-mention))))
       (catch Exception e
         (debug-println "ERROR: Issues with loading " commit-hash e))))

(defn load-tag-into-titan [{:keys [tagger object tag-name date] :as tag}]
  (debug-println "INFO: Tag being loaded is " tag-name
                 (swap! tag-index inc) "/" @total-tags)
  (try
    (g/retry-transact!
     4 (fn [i] (* i i 100))
     (let [object-node    (first (v/upsert! :commit-hash object))
           tagger-node    (when (and (:name tagger) (:email tagger))
                            (first (v/upsert! :name tagger)))
           tag-node       (first (v/upsert! :tag-name {:type "tag"
                                                       :tag-name tag-name
                                                       :date date}))]
       (when tagger-node (e/upconnect! tagger-node tag-node "authored"))
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
