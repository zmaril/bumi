(ns linux-titan.titan
  (:use [clojure.java.shell :only (sh with-sh-dir)]))

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

