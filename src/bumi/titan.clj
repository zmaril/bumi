(ns bumi.titan
  (:use     [bumi.config :only (graph-config)])
  (:require [hermes.core :as g]
            [hermes.vertex :as v]
            [hermes.edge :as e]
            [hermes.type :as t]))

(defn filtered-upsert! [ks m]
  "Given a list of keys and a property map, filtered-upsert! either
   creates a new node with that property map or updates all nodes with
   the given key value pairs to have the new properties specifiied by
   the map. Always returns the set of vertices that were just update
   or created. Uses the first value of ks as an index look up."
  ;;(ensure-graph-is-transaction-safe)
  (let [vertices (v/find-by-kv (first ks) ((first ks) m))
        filtered (filter (fn [vertex]
                           (every? #(= (% m) (v/get-property vertex %)) ks)) vertices)]
    (if (empty? vertices)
      (set [(v/create! m)])
      (do
        (doseq [vertex vertices] (v/set-properties! vertex m))
        vertices))))

(defn start []
  (println "Opening titan...")  
  (g/open graph-config)
  (println "Checking for keys and labels...")
  (g/transact!
   ;;Types
   (t/create-vertex-key-once :type String {:functional true})
   ;;Person types
   (t/create-vertex-key-once :name String
                             {:indexed true
                              :functional true})   
   (t/create-vertex-key-once :email String
                             {:indexed true
                              :functional true})
   ;;Commit types
   (t/create-vertex-key-once :commit-hash String
                             {:indexed true
                              :unique true
                              :functional true})
   
   (t/create-vertex-key-once :message String {:functional true})
   ;;File types
   (t/create-vertex-key-once :file String
                             {:indexed true
                              :unique true
                              :functional true})   
   ;;File types
   (t/create-vertex-key-once :tag-name String
                             {:indexed true
                              :unique true
                              :functional true})   

   ;;Labels
   ;;Commit -> Person
   (let [group (t/create-group 2 "mentioned")]
     (t/create-edge-label-once :Reviewed-by {:group group})
     (t/create-edge-label-once :Reported-by {:group group})
     (t/create-edge-label-once :Tested-by {:group group})
     (t/create-edge-label-once :Acked-by {:group group})
     (t/create-edge-label-once :From {:group group}))   
   ;;Person -> Commit
   (t/create-edge-label-once :committed)
   (t/create-edge-label-once :authored)
   (t/create-vertex-key-once :committed {:functional true})   
   ;;Commit -> commit
   (t/create-edge-label-once :parent-of)   
   ;;Commit -> File
   (t/create-edge-label-once :changed)
   (t/create-vertex-key-once :new? Boolean {:functional true})
   (t/create-vertex-key-once :diff String  {:functional true}))  
  (println "All set up! WAAHHOOOOOO!"))