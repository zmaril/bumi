(ns bumi.core
  (:require [bumi.git :as git]
            [bumi.titan :as titan]
            [hermes.core :as g]
            [hermes.vertex :as v]
            [hermes.edge :as e]))

;;All of the connect-commit-to[x] happen inside of a transact! anyway
(defn connect-commit-to-diff [commit-node diff]
  (let [diff-node (first (v/upsert! :filename {:filename (:file diff)}))]
    (e/upconnect! commit-node diff-node "changed" (dissoc diff :file))))

(defn connect-commit-to-parent [commit-node parent-hash]
  (let [parent-node (first (v/upsert! :hash {:hash parent-hash}))]
    (e/upconnect! commit-node parent-node "child-of")))

(defn connect-commit-to-people-mentioned
  [commit-node [key people]]
  (doseq [person people]
    (let [person-node (first (v/upsert! :name (dissoc person :date :timezone)))]
      (e/upconnect! commit-node person-node (name key)))))

(defn load-commit-into-titan [{:keys [people-mentioned diffs message
                                      hash parents author committer] :as commit}]
  (g/retry-transact!
   4 (fn [i] (* i i 100) )
   (let [author-node    (first (titan/filtered-upsert! [:name :email]
                                                       (assoc author   :type "person")))
         committer-node (first (titan/filtered-upsert! [:name :email]
                                                       (assoc committer :type "person")))
         commit-node    (first (v/upsert! :hash {:type "commit"
                                                 :hash hash
                                                 :message message}))]
     (e/upconnect! author-node commit-node "authored")
     (e/upconnect! committer-node commit-node "committed")
     (doseq [diff diffs]
       (connect-commit-to-diff commit-node diff))
     (doseq [parent parents]
       (connect-commit-to-parent commit-node parent))
     (doseq [type-of-mention people-mentioned]
       (connect-commit-to-people-mentioned commit-node type-of-mention)))))

(defn load-tag-into-titan [{:keys [name tagger commit] :as tag}]
  (g/retry-transact!
   4 (fn [i] (* i i 100))
   nil))

(defn -main []
  (titan/start)
  (doall (pmap (comp load-commit-into-titan git/parse-commit-from-hash)
               (git/git-rev-list)))
  (doall (pmap (comp load-tag-into-titan git/parse-tag-from-name)
               (git/git-tag-list)))
  (println "Success")
  (System/exit 0))
