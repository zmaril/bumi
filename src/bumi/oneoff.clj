(ns bumi.oneoff
  (:require [bumi.titan :refer (start)]
            [clojurewerkz.titanium.graph :as g]
            [clojurewerkz.titanium.vertices :as v]
            [clojurewerkz.titanium.edges :as e]
            [clojurewerkz.titanium.types :as t]
            [ogre.core :as q]))

;;TODO should passing in labels be a []? 
(defn degree-of [v & labels]
  (q/query v
           (#(apply q/<-> (cons % labels)))
           q/count!))

(defn degree-distribution []
  (map degree-of (v/get-all-vertices)))


(defn get-all-edges []
  (seq (.getEdges (g/get-graph))))

(defn create-chain [n]
  (let [vs (for [i (range n)] (v/create! {:i i}))]
    (doseq [i (range (dec n))]
      (e/connect! (nth vs i) :connected (nth vs (inc i))))))

(defn create-complete [k]
  (let [vs (for [i (range k)] (v/create! {:i i}))]
    (for [i vs j vs :when (not= i j)]
      (e/connect! i :connected j))))

(defn power-law [n]
  (loop [i 1
         vst [(v/create! {:i 0})]]     
    (when (< i n)
      (let [new-vertex (v/create! {:i i})
            old-vertex (rand-nth vst)]
        (e/connect! new-vertex :connected old-vertex)        
        (recur (inc i) (conj vst new-vertex ))))))

(defn seq->string-for-R [col]
  (apply str (interleave col (cycle ["\n"]))))

(defn spit-seq [filename col]
  (spit filename (seq->string-for-R col)))

(defn setup []
  (g/open {"storage.backend" "inmemory"})
  (g/transact! (power-law 100000)))

(defn run []
  (spit-seq "powerlaw.txt" (g/transact! (seq (doall (degree-distribution))))))
