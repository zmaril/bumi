(ns bumi.analysis
  (:use     [hermes.gremlin :only (query)]
            [clojure.string :only (join)])
  (:require [me.raynes.conch.low-level :as sh]
            [hermes.core :as g]            
            [hermes.vertex :as v]
            [hermes.edge :as e]
            [hermes.type :as t]))

(def people (-> g/*graph* (.getVertices "type" "person") seq))
(def commits (-> g/*graph* (.getVertices "type" "commit") seq))
(def files (-> g/*graph* (.getVertices "type" "file") seq))

(defn starr [& args]
  (into-array String args))

(defn find-degree [v & types]
  (query v
         (both (apply starr types))
         count))



(defn analysis-label-degrees []
  (doseq [label ["authored" "committed" "Cc" "Signed-off-by" "Acked-by" "Reported-by"]]
    (spit (str "output/authors-" label ".txt")
          (->> (seq (frequencies (map #(find-degree % label) people)))
               (sort-by first)
               (map (partial join " "))
               (join "\n"))))
  (sh/proc "R" "CMD" "BATCH" "src/R/authors.R"))

(defn analysis-file-degrees []
  (spit (str "output/file-degrees.txt")
        (->> (seq (frequencies (map #(find-degree % "") files)))
             (sort-by first)
             (map (partial join " "))
             (join "\n")))
  (sh/proc "R" "CMD" "BATCH" "src/R/authors.R"))

