(ns linux-titan.git
  (:use [clojure.java.shell :only (sh with-sh-dir)]
        [clojure.string :only (join split trim)]
        ))

(defn show-commit
  "Given a SHA-1 hash, show-commit fecthes the raw commit from git."
  [hash]  
  (with-sh-dir "/Users/zackmaril/Projects/experiments/linux"
    (sh "git" "show" hash "--no-abbrev-commit" "--format=raw")))

(defn produce-rev-list
  "Given a source directory, this fn produes the entire list of
  commits on the current branch."
  [dir]
  (-> (with-sh-dir dir
        (sh "git" "rev-list" "--remotes" "-n 200"))
      (:out)
      (clojure.string/split #"\n")))

(defn begins-with-n-spaces?
  "Predicate asking whether s begins with n spaces."
  [n s]
  (=  (take n (iterate (constantly \space) \space))
      (take n s)))

(defn begins-with-this-string?
  "Predicate asking whether this string begins with s string."
  [this s]
  (= this (join (take (count this) s))))

(defn str-drop
  "Dumbply drops t from s."
  [s t] (join (drop (count s) t)))


(defn parse-name
  "Given a line like the following:
   Linus Torvalds <torvalds@linux-foundation.org> 1359507746 +1100
   It returns a map with the author's name, email, and date.
   If the date isn't included, then it will be nil."
  [line]
  (let [[name,rest-of-line] (split-with (complement #{\<}) line)
        [email,date-raw] (split-with (complement #{\>}) (join rest-of-line))
        date-str (join (take-while (partial not= \space)
                                   (trim (join (drop 2 date-raw)))))
        date (when (not (empty? date-str))
               (java.util.Date. (* 1000 (Integer/parseInt date-str))))]
    
    {:name  (trim (join name)),
     :email (trim (join (drop 1 email)))
     :date date}))

(defn parse-headers
  "Given a list of the headers of the commit message, this parses and
  formats into a map."
  [[commit tree & others]]
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

(defn parse-change [line]
  line)

(def commit-message-metainfo #{"Signed-off-by" "Cc" "Reported-by" "Acked-by"
                               "Tested-by" "Reviewed-by" "From"})

(defn parse-body [body]
  (let [[message-lines diff-raw] (split-with (partial begins-with-n-spaces? 4) body)
        
        cleaned-message-lines (map clojure.string/trim message-lines)
        finder (fn [meta]
                 (let [key (keyword meta)
                       selected-lines (filter
                                       (partial begins-with-this-string? meta)
                                       cleaned-message-lines)
                       parsed-lines (map (comp parse-name
                                               (partial str-drop (str meta ": ")))
                                         selected-lines)]
                   {key parsed-lines}))        
        metainfo (map finder commit-message-metainfo)

        diffs (map parse-change (drop 1 (split (join "\n" diff-raw ) #"\ndiff --git ")))]
    {:message (filter (complement commit-message-metainfo)
                      cleaned-message-lines)
     :diffs diffs
     :people-mentioned (merge (apply merge metainfo))}))

(defn parse-commit-from-hash
  "From the hash, this goes and parses the commit into a usable
  format." [hash]
  (let [raw-commit (-> hash show-commit :out)
        lines (clojure.string/split raw-commit #"\n")
        [headers, body] (split-with (complement (partial begins-with-n-spaces? 4)) lines)]    
    (merge (parse-headers headers) (parse-body body))))
