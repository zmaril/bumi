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

(defn twod-list-to-str [lst]
          (->> lst
               (sort-by first)
               (map (partial join " "))
               (join "\n")))

(defn analyze-label-degrees []
  (doseq [label ["authored" "committed" "Cc" "Signed-off-by" "Acked-by" "Reported-by"]]
    (spit (str "output/authors-" label ".txt")
          (twod-list-to-str (seq (frequencies (map #(find-degree % label) people))))))
  (sh/proc "R" "CMD" "BATCH" "src/R/authors.R"))

(defn analyze-number-of-files-changed []
  (spit "output/number-of-files-changed.txt"
        (join "\n" (map #(find-degree % "changed") commits)))
  (sh/proc "R" "CMD" "BATCH" "src/R/commits.R"))

(defn analyze-all-the-things []
  (analyze-label-degrees)
  (analyze-number-of-files-changed))

(defn diff-lengths []
  (let [diffs (query commits (bothE (starr "changed")) (property "diff"))
        freqs (frequencies (map count diffs))]
    freqs))

