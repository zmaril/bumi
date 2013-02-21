(ns bumi.faunus
  (:use [clojure.java.shell :only (sh with-sh-dir)]
        [bumi.config :only (graph-config)])
  (:import (com.thinkaurelius.faunus FaunusGraph FaunusPipeline)))

(def faunus-conf
  {"faunus.graph.input.format" "com.thinkaurelius.faunus.formats.graphson.GraphSONInputFormat"
   "faunus.input.location" "graph-of-the-gods.json"
   "faunus.graph.output.format" "com.thinkaurelius.faunus.formats.graphson.GraphSONOutputFormat"
   "faunus.sideeffect.output.format" "org.apache.hadoop.mapreduce.lib.output.TextOutputFormat"
   "faunus.output.location" "output"
   "faunus.output.location.overwrite" "true"})

(defn convert-config-map [m]
  (let [conf (org.apache.hadoop.conf.Configuration.)]
    (doseq [[k1 v1] m] (.set conf k1 v1))
    conf))

(def faunus-graph (FaunusGraph. (convert-config-map faunus-conf)))
(def faunus-pipe (FaunusPipeline. faunus-graph))

(defn start-analysis []
  (.. faunus-pipe V count submit))
