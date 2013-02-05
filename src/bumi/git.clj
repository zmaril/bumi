  (:require [clojure.string :as s])
        [bumi.config :only (git-root-dir)]))
(defn git [& cmds]
  (->> (apply sh (cons "git" cmds))
       (with-sh-dir git-root-dir)
       (:out)))

(defn git-show-object
  "Given a SHA-1 hash, git-show-commit fetchs the raw object from git."
  [hash]
  (git "show" hash "--no-abbrev-commit" "--format=raw"))
  []
  (-> (git "rev-list" "--all" "-n200")
      (s/split #"\n")))
(defn produce-tag-list
  "Given a source directory, this fn produes the entire list of
  commits on the current branch."
  []
  (-> (git "tag")
      (s/split #"\n")))
  "Given a line like the following: Linus Torvalds
   <torvalds@linux-foundation.org> 1359507746 +1100
   It returns a map with the author's name, email,
   and date. If the date isn't included, then it will
   be nil."
        [email,date-raw] (split-with (complement #{\>}) (s/join rest-of-line))
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
  (let [parents (map #(s/replace-first "parent " % "")
                     (filter (partial re-find #"^parent") others))
        author-line    (first (filter (partial re-find #"^author") others))
        author (parse-name (s/replace-first "author " author-line ""))
        committer-line (first (filter (partial re-find #"^committer") others))
        committer (parse-name (s/replace-first "committer " committer-line ""))] 
    {:hash  (s/replace-first "commit " commit "")
     :tree    (s/replace-first "tree " tree "")
  (let [lines (s/split raw #"\n")
                 (s/split #" ")
                 s/join)
        new? (boolean (re-find #"^new file mode" (second lines)))]
     :diff (s/join "\n" (drop-while (complement (partial re-find "^@@")) lines)) 
  (let [[message-lines diff-raw] (split-with (partial re-find #"^    ") body)
        cleaned-message-lines (map s/trim message-lines)
                                       (partial re-find (re-pattern (str "^" meta)))
                                               #(s/replace-first (str meta ": ") % ""))
        diffs (map parse-diff (drop 1 (s/split (s/join "\n" diff-raw ) #"\ndiff --git ")))]
    {:message (s/join "\n" (filter (fn [s]
                                     (fn [t] (re-find (re-pattern (str t ":")) s))
  format."
  [hash]
  (let [raw-commit (git-show-object hash)
        lines (s/split raw-commit #"\n")
        [headers, body] (split-with (complement (partial re-find #"^    ")) lines)]    