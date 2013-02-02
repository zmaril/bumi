(ns bumi.core
  (:require [bumi.git :as git]
            [bumi.titan :as titan]
            [hermes.core :as g]
            [hermes.vertex :as v]
            [hermes.edge :as e]))

(def linux-src-dir "/Users/zackmaril/Projects/experiments/linux")

;;All of the connect-commit-to[x] happen inside of a transact! anyway
(defn connect-commit-to-diff [commit-node diff]
  (let [diff-node (first (v/upsert! :filename {:filename (:file diff)}))]
    (e/upconnect! commit-node diff-node "changed" (dissoc diff :file))))

(defn connect-commit-to-parent [commit-node parent-hash]
  (let [parent-node (first (v/upsert! :hash {:hash parent-hash}))]
    (e/upconnect! commit-node parent-node "child-of")))

(defn connect-commit-to-people-mentioned
  [commit-node [key people]]
;;  (println key people)
  )

(defn load-commit-into-titan [{:keys [people-mentioned diffs message
                                      hash parents author committer] :as commit}]
  (g/transact!
   (let [author-node    (first (v/upsert! :name (assoc author   :type "person")))
         committer-node (first (v/upsert! :name (assoc committer :type "person")))
         commit-node    (first (v/upsert! :hash {:type "commit"
                                                 :hash hash
                                                 :message message}))]
     (e/upconnect! author-node commit-node "authored")
     (e/upconnect! committer-node commit-node "committed")
     (doseq [diff diffs]
       (connect-commit-to-diff commit-node diff))
     (doseq [parent parents]
       (connect-commit-to-parent commit-node parent))
     (doseq [person people-mentioned]
       (connect-commit-to-people-mentioned commit-node person))))
  (println "Loaded " hash))

(defn -main []
  (titan/start)
  (let [rev-list (git/produce-rev-list linux-src-dir)
        commits (map git/parse-commit-from-hash rev-list)]
    (doseq [commit commits] (load-commit-into-titan commit)))
  (println "Success")
  (System/exit 0))
