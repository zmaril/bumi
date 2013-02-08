(ns bumi.git
  (:require [clojure.string :as s]
            [me.raynes.conch.low-level :as sh])
  (:use [clojure.java.shell :only (sh with-sh-dir)]
        [bumi.config :only (git-root-dir)]))

(defn git-rev-list
  "Given a source directory, this fn produes the entire list of
  commits, regardless of branch and tags." []
  (-> (sh/proc "git" "rev-list" "--all" "-n2000" :dir git-root-dir)
                  (sh/stream-to-string :out)
                  s/split-lines))

(def tag-list (-> (sh/proc "git" "tag" :dir git-root-dir)
                  (sh/stream-to-string :out)
                  s/split-lines))

(def commit-message-metainfo #{"Signed-off-by" "Cc" "Reported-by" "Acked-by"
                               "Tested-by" "Reviewed-by" "From"})

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

(defn parse-diff [lines]
  (let [file (-> (first lines)
                 (s/split #" ")
                 first
                 ((partial drop 2))
                 s/join)
        new? (boolean (re-find #"^new file mode" (second lines)))]
    {:file file
     :new? new?
     :diff (s/join "\n" (drop-while (complement (partial re-find "^@@")) lines))}))

(defn parse-commit-body [body]
  (let [[message-lines diff-raw] (split-with (complement (partial re-find #"^diff --git")) body)
        cleaned-message-lines (map s/trim message-lines)
        finder (fn [meta]
                 ;;TODO: Precompute these regexes before hand, make
                 ;;them be waiting around
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

(def format-str (s/join "%n"
                           ["%H"  ;;commit-hash
                            "%T"  ;;tree-hash
                            "%P"  ;;parent-hash
                            "%aN" ;;author-name
                            "%aE" ;;author-email
                            "%aD" ;;author-date, TODO: which date?
                            "%cN" ;;author-name
                            "%cE" ;;author-email
                            "%cD" ;;author-date, TODO: which date?                            
                            "%b"  ;;body of the message
                            ]))

(defn parse-commit-from-hash
  "From the hash, this goes and parses the commit into a usable
  format."
  [hash]
  (let [[commit-hash tree-hash parents
         author-name author-email author-date
         committer-name committer-email committer-date
         & body]
        (-> (sh/proc "git" "show" (str "--format=" format-str)
                     hash
                     :dir git-root-dir)
            (sh/stream-to-string :out)
            s/split-lines)

        header-information     {:commit-hash commit-hash
                                :tree        tree-hash
                                :parents     (s/split parents #" ")
                                :author      {:name  author-name
                                              :email author-email
                                              :date (java.util.Date. author-date)}
                                :committer   {:name  committer-name
                                              :email committer-email
                                              :date  (java.util.Date. committer-date)}}        
        message-information (parse-commit-body body)]
    (merge header-information
           message-information)))

;;TODO This currently has some trouble with the very beginning of the
;;linux kernel history.
(defn parse-tag-name [tag-name]
  (let [[_ tagger-line & other-lines] (-> (sh/proc "git" "show" "-s" tag-name
                                                   :dir git-root-dir)
                                          (sh/stream-to-string :out)
                                          s/split-lines)
        [tagger-name, tagger-email]   (-> (s/replace-first tagger-line "Tagger: " "")
                                          (s/split #"[<>]")
                                          (#(map s/trim %)))
        ;;TODO Handle the case when there is no tag more gracefully
        date   (try
                 (-> (filter (partial re-find #"^Date:") other-lines)
                     first
                     (s/replace-first "Date: " "")
                     (java.util.Date.))                 
                 (catch Exception e
                   (println e)
                   nil))
        ;;TODO Handle the case when the tags don't point to
        ;;commits, like when they are trees.
        commit-hash   (try
                        (-> (filter (partial re-find #"^commit") other-lines)
                            first                             
                            (s/replace-first "commit " ""))
                        (catch Exception e
                          (println e)
                          nil))]
    {:tagger {:name tagger-name
              :email tagger-email}
     :object {:hash commit-hash
              :type :commit} 
     :tag-name tag-name
     :date date}))
