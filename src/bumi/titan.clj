(ns bumi.titan
  (:use     [bumi.config :only (graph-config)])
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
   ;;"person","commit","file","tag"
   (t/create-vertex-key-once :type String
                             {:functional true
                              :indexed true})
   ;;Person name
   ;;"Linus Torvalds"
   (t/create-vertex-key-once :name String
                             {:indexed true
                              :unique true
                              :functional true})
   ;;Person email
   ;;"linus@torvalds.org"
   ;;Note: Do not make this unique. It will break everything because
   ;;of the following. 
   ;;INFO: Hash being loaded is  b9149729ebdcfce63f853aa54a404c6a8f6ebbf3   18 / 349693
   ;;author {:name Ian Campbell, :email Ian.Campbell@citrix.com, :date 1360194098000, :type person}
   ;;committer {:name David S. Miller, :email davem@davemloft.net, :date 1360297769000, :type person}
   ;;Mentioned {:name Jan Beulich, :email JBeulich@suse.com, :type person, :date nil, :timezone nil}
   ;;Mentioned {:name Ian Campbell, :email ian.campbell@citrix.com, :type person, :date nil, :timezone nil}

   (t/create-vertex-key-once :email String
                             {:indexed true

                              :functional true})
   ;;Commit hash
   ;;"990c6bebda133c5f0b1d33682245f611ffc02e6b"
   (t/create-vertex-key-once :hash String
                             {:indexed true
                              :unique true                              
                              :functional true})
   ;;Commit message
   ;;"Example message"
   (t/create-vertex-key-once :message String
                             {:functional true})
   ;;File name
   ;;"/src/projects.clj"
   (t/create-vertex-key-once :filename String
                             {:indexed true
                              :unique true                              
                              :functional true})   
   ;;Tag name
   ;;"rc-0.14"
   (t/create-vertex-key-once :tag-name String
                             {:indexed true
                              :unique true                              
                              :functional true})   

   ;;Commit -> Person
   (let [mentioned (t/create-group 2 "mentioned")]
     (t/create-edge-label-once :reviewed-by {:group mentioned})
     (t/create-edge-label-once :reported-by {:group mentioned})
     (t/create-edge-label-once :tested-by   {:group mentioned})
     (t/create-edge-label-once :acked-by    {:group mentioned})
     (t/create-edge-label-once :from        {:group mentioned}))   
   ;;Person -> Commit
   (t/create-edge-label-once :committed)
   (t/create-edge-label-once :authored)
   ;;When the person authored or committed the commit. 
   (t/create-vertex-key-once :date Long {:functional true})
   ;;Commit -> Commit
   (t/create-edge-label-once :child-of)   
   ;;Commit -> File
   (t/create-edge-label-once :changed)
   (t/create-vertex-key-once :new? Boolean {:functional true})
   (t/create-vertex-key-once :diff String  {:functional true}))  
  (println "All set up! WAAHHOOOOOO!"))