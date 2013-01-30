(ns linux-titan.git
  (:use [clojure.java.shell :only (sh with-sh-dir)]))


(defn show-commit [hash]
  (with-sh-dir "/Users/zackmaril/Projects/experiments/linux"
    (sh "git" "show" hash "--no-abbrev-commit" "--format=raw")))

(defn produce-rev-list [dir]
  (-> (with-sh-dir dir
        (sh "git" "rev-list" "--remotes" "-n 20"))
      (:out)
      (clojure.string/split #"\n")))

(defn begins-with-n-spaces? [n s]
  (=  (take n (iterate (constantly \space) \space))
      (take n s)))

(defn begins-with-this-string? [this s]
  (= this
     (apply str (take (count this) s))))

(defn format-headers [[commit tree & others]]
  {:commit (drop (count "commit ") commit)
   :tree   (drop (count "tree ") tree)
   :parents (take-while #(= "parent" (take 6 %)) others)
   :author
   :authored
   :commiter
   :commited
   })

(defn format-body [])

(defn format-commit-from-hash [hash]
  (let [raw-commit (-> hash show-commit :out)
        lines (clojure.string/split raw-commit #"\n")
        [headers, body] (split-with (complement (partial begins-with-n-spaces? 4)) lines)]
    (merge (formart-headers headers) (format-body body))))
