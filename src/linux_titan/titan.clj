(ns linux-titan.titan
  (:use [clojure.java.shell :only (sh with-sh-dir)])
  (:require [hermes.core :as g]
            [hermes.vertex :as v]
            [hermes.edge :as e]
            [hermes.type :as t]))

;;Person structure 
{
 :name String
 :email String
}

;;Person-[:authored]>commit
{
 :authored Date
}

;;Person-[:commited]->commit
{
 :commited Date
}

;;Person-[:reported]->commit
{}
;;Person-[:cc]->commit
{}
;;Person-[:acked]->commit
{}
;;Person-[:signed-off]->commit
{}

;;Commit structure
{
 :commit String
 :message String

 :tree String ;;TODO: Should this be included?
}

;;Commit -[:parent-of]-> Commit
{}

;;Commit -[:changed] -> File
{
 :diff String
}

;;File structure
{
 :filename String 
 }

;;;START STUFFING THIS INTO TITAN, forget about diffs as much. Just
;;;get which file they are pointing to. 