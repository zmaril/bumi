(ns bumi.git
  (:require [clojure.string :as s]
            [clj-jgit.porcelain :as porc]
            [clj-jgit.querying :as quer]
            [bumi.config :refer (debug-println graph-config git-root-dir)]))

(def repo (porc/load-repo git-root-dir)) 
(def rev-list (take 10 (porc/git-log repo)))
(def commit-message-metainfo #{"Signed-off-by" "Cc" "Reported-by" "Acked-by"
                               "Tested-by" "Reviewed-by" "From"})

(defn PersonIdent->map [person]
  {:name  (.getName person)
   :email (.getEmailAddress person)
   :timezone (.getTimeZoneOffset person)
   :when (.getWhen person)
   :type "person"})

(defn parse-name
  "Given a line like the following:

   Linus Torvalds <torvalds@linux-foundation.org> 1359507746 +1100

   It returns a map with the author's name, email, and date. If the
   date isn't included or isn't in the correct format, then date will
   be nil." 
  [line]
  (let [[name,rest-of-line] (split-with (partial not= \<) line)
        [email,date-raw] (split-with (partial not= \>) (s/join rest-of-line))]
    ;;This email is tough to parse
    ;;stable <stable@vger.kernel.org> [v3.3]  
    {:name  (s/trim (s/join name)),
     :email (s/trim (s/join (drop 1 email)))
     :type "person"}))

(defn parse-message [m]
  (let [cleaned-message-lines (map s/trim (s/split-lines m))
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
        metainfo (map finder commit-message-metainfo)]
    [(->> cleaned-message-lines
          (filter (fn [s] (not-any?
                           #(re-find (re-pattern (str % ":")) s)
                           commit-message-metainfo)))
          (s/join "\n"))
     (apply merge metainfo)]))
        
;;Modifed from orginal code
;;https://github.com/clj-jgit/clj-jgit/blob/master/src/clj_jgit/querying.clj
(defn RevCommit->map [rev-commit]
  (let [[message, mentions] (parse-message (.getFullMessage rev-commit))]
    {:id (.getName rev-commit)
     :author    (PersonIdent->map (.getAuthorIdent rev-commit))
     :committer (PersonIdent->map (.getCommitterIdent rev-commit))
     :message message
     :mentions mentions
     :changed_files (quer/changed-files repo rev-commit)
     :parents (map (memfn getName) (.getParents rev-commit))}))

