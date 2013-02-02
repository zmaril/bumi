(ns linux-titan.git
  (:use [clojure.java.shell :only (sh with-sh-dir)]
        [clojure.string :only (join split trim)]
        ))

(defn show-commit
  "Given a SHA-1 hash, show-commit fecthes the raw commit from git."
  [hash]  
  (with-sh-dir "/Users/zackmaril/Projects/experiments/linux"
    (sh "git" "show" hash "--no-abbrev-commit" "--format=raw")))

(defn produce-rev-list [dir]
  (-> (with-sh-dir dir
        (sh "git" "rev-list" "--remotes" "-n 200"))
      (:out)
      (clojure.string/split #"\n")))

(defn begins-with-n-spaces? [n s]
  (=  (take n (iterate (constantly \space) \space))
      (take n s)))

(defn begins-with-this-string? [this s]
  (= this (join (take (count this) s))))

(defn str-drop [s t] (join (drop (count s) t)))


(defn parse-name [line]
  (let [[name,rest-of-line] (split-with (complement #{\<}) line)
        [email,date-raw] (split-with (complement #{\>}) (join rest-of-line))
        date-str (join (take-while (partial not= \space)
                                   (trim (join (drop 2 date-raw)))))
        date (when (not (empty? date-str))
               (java.util.Date. (* 1000 (Integer/parseInt date-str))))]
    
    {:name  (trim (join name)),
     :email (trim (join (drop 1 email)))
     :date date}))

(defn format-headers [[commit tree & others]]
  (let [parents (map (partial str-drop "parent ")
                     (filter (partial begins-with-this-string? "parent") others))
        
        author-line    (first (filter (partial begins-with-this-string? "author") others))
        author (parse-name (str-drop "author " author-line))
        
        committer-line (first (filter (partial begins-with-this-string? "committer") others))
        committer (parse-name (str-drop "committer " committer-line))] 
    {:commit  (str-drop "commit " commit)
     :tree    (str-drop "tree " tree)
     :parents parents
     :author author
     :committer   committer}))

(defn format-change [line]
  line)

(def commit-message-metainfo ["Signed-off-by" "Cc" "Reported-by" "Acked-by"
                              "Tested-by" "Reviewed-by" "From"])

(defn format-body [body]
  (let [[message-lines diff-raw] (split-with (partial begins-with-n-spaces? 4) body)
        
        cleaned-message-lines (map clojure.string/trim message-lines)
        finder (fn [meta]
                 (let [key (keyword meta)
                       selected-lines (filter
                                       (partial begins-with-this-string? meta)
                                       cleaned-message-lines)
                       formatted-lines (map (comp parse-name
                                                  (partial str-drop (str meta ": ")))
                                            selected-lines)]
                   {key formatted-lines}))        
        metainfo (map finder commit-message-metainfo)

        diffs (map format-change (drop 1 (split (join "\n" diff-raw ) #"\ndiff --git ")))]
    (merge {:message cleaned-message-lines :diff diffs}
           (apply merge metainfo))))

(defn format-commit-from-hash [hash]
  (let [raw-commit (-> hash show-commit :out)
        lines (clojure.string/split raw-commit #"\n")
        [headers, body] (split-with (complement (partial begins-with-n-spaces? 4)) lines)]    
    (merge (format-headers headers) (format-body body))))
