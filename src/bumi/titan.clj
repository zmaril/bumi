(ns bumi.titan
  (:use [clojure.java.shell :only (sh with-sh-dir)]
        [bumi.config :only (graph-config)])
  (:require [hermes.core :as g]
            [hermes.vertex :as v]
            [hermes.edge :as e]
            [hermes.type :as t]))

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
   (t/create-vertex-key-once :hash String
                             {:indexed true
                              :unique true
                              :functional true})
   (t/create-vertex-key-once :message String {:functional true})
   ;;File types
   (t/create-vertex-key-once :filename String
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