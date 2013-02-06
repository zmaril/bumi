(ns bumi.git
  (:require [clojure.string :as s])
  (:use [clojure.java.shell :only (sh with-sh-dir)]
        [bumi.config :only (git-root-dir)]))

(defn git
  "git acts just like the git shell. Supply lines and it does things for you."
  [& cmds]
  (->> (apply sh (cons "git" cmds))
       (with-sh-dir git-root-dir)
       (:out)))

(defn git-show-object
  "Given a SHA-1 hash, git-show-object fetchs the raw object from git."
  [hash]
  (git "show" hash "--format=raw"))

(defn git-rev-list
  "Given a source directory, this fn produes the entire list of
  commits, regardless of branch and tags." []
  (s/split-lines (git "rev-list" "--all" "-n2000")))

(defn git-tag-list
  "Given a source directory, this fn produes the tags for the
  project." []
  (s/split-lines (git "tag")))

(defn parse-name
  "Given a line like the following: Linus Torvalds
   <torvalds@linux-foundation.org> 1359507746 +1100 It returns a map
   with the author's name, email, and date. If the date isn't included
   or isn't in the correct format, then date will be nil."
  [line]
  (let [[name,rest-of-line] (split-with (partial not= \<) line)
        [email,date-raw] (split-with (partial not= \>) (s/join rest-of-line))
        [_, date-str, timezone] (s/split (s/join date-raw) #" ")]
    {:name  (s/trim (s/join name)),
     :email (s/trim (s/join (drop 1 email)))
     ;;This email is tough to parse
     ;;stable <stable@vger.kernel.org> [v3.3]
     :date (try (when (not (empty? date-str))
                  (java.util.Date. (* 1000 (Integer/parseInt date-str))))
                (catch Exception e
                  (println e)
                  (println line) nil))
     :timezone timezone}))

(defn parse-headers
  "Given a list of the headers of the commit message, this parses and
  formats into a map."
  [[commit tree & others]]
  (let [parents        (map #(s/replace-first % "parent " "")
                            (filter (partial re-find #"^parent") others))
        
        author-line    (first (filter (partial re-find #"^author") others))
        author         (parse-name (s/replace-first author-line "author " ""))
        
        committer-line (first (filter (partial re-find #"^committer") others))
        committer      (parse-name (s/replace-first committer-line "committer " ""))] 
    {:hash      (s/replace-first commit "commit " "")
     :tree      (s/replace-first  tree "tree " "")
     :parents   parents
     :author    author
     :committer committer}))

(defn parse-diff [raw]
  (let [lines (s/split-lines raw)
        file (-> (first lines)
                 (s/split #" ")
                 first
                 ((partial drop 2))
                 s/join)
        new? (boolean (re-find #"^new file mode" (second lines)))]
    {:file file
     :new? new?
     :diff (s/join "\n" (drop-while (complement (partial re-find "^@@")) lines))}))

(def commit-message-metainfo #{"Signed-off-by" "Cc" "Reported-by" "Acked-by"
                               "Tested-by" "Reviewed-by" "From"})

(defn parse-body [body]
  (let [[message-lines diff-raw] (split-with (partial re-find #"^    ") body)
        cleaned-message-lines (map s/trim message-lines)
        finder (fn [meta]
                 ;;TODO: Precompute these regexes before hand, make
                 ;;them be watching around
                 (let [selected-lines (filter
                                       (partial re-find (re-pattern (str "^" meta)))
                                       cleaned-message-lines)
                       parsed-lines (map (comp parse-name
                                               #(s/replace-first % (str meta ": ") ""))
                                         selected-lines)]
                   {(keyword meta) parsed-lines}))        
        metainfo (map finder commit-message-metainfo)
        diffs (map parse-diff (drop 1 (s/split (s/join "\n" diff-raw ) #"\ndiff --git ")))]
    {:message (->> cleaned-message-lines
                   (filter (fn [s] (not-any?
                                    #(re-find (re-pattern (str % ":")) s)
                                    commit-message-metainfo)))
                   (s/join "\n")) 
     :diffs diffs
     :people-mentioned (apply merge metainfo)}))

(defn parse-commit-from-hash
  "From the hash, this goes and parses the commit into a usable
  format."
  [hash]
  (let [raw-commit (git-show-object hash)
        lines (s/split-lines raw-commit)
        [headers, body] (split-with (complement (partial re-find #"^    ")) lines)]    
    (merge (parse-headers headers) (parse-body body))))

(defn parse-tag-from-name
  "From the hash, this goes and parses a tag into a usable
  format."
  [name]
  (let [raw-tag (git-show-object name)
        lines (s/split-lines raw-tag)]    
    nil))