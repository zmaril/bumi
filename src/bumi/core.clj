(ns bumi.core
  (:require [bumi.git :as git]
            [bumi.titan :as titan]
            [hermes.core :as g]
            [hermes.vertex :as v]
            [hermes.edge :as e]))

(def linux-src-dir "/Users/zackmaril/Projects/experiments/linux")

(def rev-list (git/produce-rev-list linux-src-dir))
(def commits (map git/parse-commit-from-hash rev-list))

(defn connect-commit-to-diff [commit diff]

  )

(defn connect-commit-to-parent [commit parent]
  
  )

(defn connect-commit-to-people-mentioned
  [commit-node [key people]]
  
  )

(defn load-commit-into-titan [{:keys [people-mentioned diffs message
                                      hash parents author committer]}]
  (g/transact! 
   (let [author-node    (first (v/upsert! :name (assoc author   :type "person")))
         committer-node (first (v/upsert! :name (assoc committer :type "person")))
         commit-node    (first (v/upsert! :hash {:type "commit"
                                                 :hash hash
                                                 :message message}))]
     (e/upconnect! author-node commit-node "authored")
     (e/upconnect! committer-node commit-node "committed")
     (doall (map (partial connect-commit-to-diff commit-node) diffs))
     (doall (map (partial connect-commit-to-parent commit-node) parents))
     (doall (map (partial connect-commit-to-people-mentioned commit-node)
                 people-mentioned)))))

(defn -main []
  (titan/start)
  (doall (pmap load-commit-into-titan commits))
  (println "Success")
  (System/exit 0))
