(ns bumi.titan
  (:use [clojure.java.shell :only (sh with-sh-dir)])
  (:require [hermes.core :as g]
            [hermes.vertex :as v]
            [hermes.edge :as e]
            [hermes.type :as t]))

(g/open "/tmp/")
;;Person structure 
{
 :name String
 :email String
}

;;Person-[:authored]>commit
{
 :authored java.util.Date
}

;;Person-[:commited]->commit
{
 :commited java.util.Date
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