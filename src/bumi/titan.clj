(ns bumi.titan
  (:use     [bumi.config :only (graph-config)])
  (:require [clojurewerkz.titanium.graph    :as g]
            [clojurewerkz.titanium.vertices :as v]
            [clojurewerkz.titanium.edges    :as e]
            [clojurewerkz.titanium.types    :as t]))


(defn start []
  (println "Opening titan...")  
  
  (g/open graph-config)
  (println "Checking for keys and labels...")
  (g/transact!
   ;;Types
   ;;"person","commit","file","tag"
   (t/create-property-key-once :type String
                               {:indexed-vertex? true
                                :unique-direction :out})
   ;;Person name
   ;;"Linus Torvalds"
   (t/create-property-key-once :name String                              
                               {:indexed-vertex? true
                                :unique-direction :out})
   ;;Person email
   ;;"linus@torvalds.org"
   ;;Note: Do not make this unique. It will break everything because
   ;;of the following. 
   ;;INFO: Hash being loaded is  b9149729ebdcfce63f853aa54a404c6a8f6ebbf3   18 / 349693
   ;;author {:name Ian Campbell, :email Ian.Campbell@citrix.com, :date 1360194098000, :type person}
   ;;committer {:name David S. Miller, :email davem@davemloft.net, :date 1360297769000, :type person}
   ;;Mentioned {:name Jan Beulich, :email JBeulich@suse.com, :type person, :date nil, :timezone nil}
   ;;Mentioned {:name Ian Campbell, :email ian.campbell@citrix.com, :type person, :date nil, :timezone nil}

   (t/create-property-key-once :email String
                               {:unique-direction :out})

   ;;Commit hash
   ;;"990c6bebda133c5f0b1d33682245f611ffc02e6b"
   (t/create-property-key-once :hash String
                               {:indexed-vertex? true
                                :unique-direction :out})
   ;;Commit message
   ;;"Example message"
   (t/create-property-key-once :message String
                               {:unique-direction :out})

   ;;File name
   ;;"/src/projects.clj"
   (t/create-property-key-once :filename String
                               {:indexed-vertex? true
                                :unique-direction :out})   
   ;;Tag name
   ;;"rc-0.14"
   (t/create-property-key-once :tag-name String
                               {:indexed-vertex? true
                                :unique-direction :out})   
   ;;Commit -> Person
   (let [mentioned (t/create-group 2 "mentioned")]
     (t/create-edge-label-once :reviewed-by {:group mentioned})
     (t/create-edge-label-once :reported-by {:group mentioned})
     (t/create-edge-label-once :tested-by   {:group mentioned})
     (t/create-edge-label-once :acked-by    {:group mentioned})
     (t/create-edge-label-once :from        {:group mentioned})
     (t/create-edge-label-once :cc        {:group mentioned}))   
   ;;Person -> Commit
   (t/create-edge-label-once :committed)
   (t/create-edge-label-once :authored)
   ;;When the person authored or committed the commit. 
   (t/create-property-key-once :date java.util.Date
                               {:indexed-edge? true
                                :unique-direction :out})
   ;;Commit -> Commit
   (t/create-edge-label-once :child-of)   
   ;;Commit -> File
   (t/create-edge-label-once :edited)
   (t/create-edge-label-once :created)
   (t/create-edge-label-once :deleted))  
  (println "All set up! WAAHHOOOOOO!"))