(ns bumi.analysis
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

(defn degree-out [v & labels]
  (q/query v
           (#(apply q/--> (cons % labels)))
           q/count!))

(defn degree-in [v & labels]
  (q/query v
           (#(apply q/<-- (cons % labels)))
           q/count!))

(defn degrees-of [v & labels]
  {:in  (apply degree-in (cons v labels))
   :out (apply degree-out (cons v labels))})

(defn degree-distribution [vs]
  (doall (map degree-of vs)))

(defn get-all-edges []
  (seq (.getEdges (g/get-graph))))

(defn seq->string-for-R [col]
  (apply str (interleave col (cycle ["\n"]))))

(defn spit-seq [filename col]
  (spit filename (seq->string-for-R col)))

(defn run []
  (spit-seq "powerlaw.txt" (g/transact! (seq (doall (degree-distribution))))))

(def mentions #{:reviewed-by :reported-by :tested-by  
                :acked-by :from :cc})         

(defn analyze-db []
  (start)
  (let [people  (g/transact! (v/find-by-kv :type "person"))
        commits (g/transact! (v/find-by-kv :type "commit"))
        files   (g/transact! (v/find-by-kv :type "file"))]
    (println (count people))
    (println (count commits))
    (println (count files))
    (println (g/transact! (frequencies (pmap #(degree-out (v/refresh %) :authored)   people))))
    (println (g/transact! (frequencies (pmap #(degree-out (v/refresh %) :committed)  people))))
    (println (g/transact! (frequencies (pmap #(degree-in  (v/refresh %) :committed)  commits))))
    (println (g/transact! (frequencies (pmap #(degree-in  (v/refresh %) :authored)   commits))))
    (println (g/transact! (frequencies (pmap #(degree-in  (v/refresh %) :committed)  files))))
    (println (g/transact! (frequencies (pmap #(degree-in  (v/refresh %) :authored)   files))))))

(q/query linus
         (q/--> :authored)
         q/count!)