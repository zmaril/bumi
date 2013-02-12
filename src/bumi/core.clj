(ns bumi.core
  (:use     [bumi.config :only (debug-println storage-dir)])
  (:require [bumi.git :as git]
            [bumi.titan :as titan]
            [hermes.core :as g]
            [hermes.vertex :as v]
            [hermes.edge :as e]
            [clojure.data.xml :as xml]))

;;All of the connect-commit-to[x] happen inside of a transact! anyway
(defn connect-commit-to-diff [commit-node diff]
  (let [diff-node (first (v/upsert! :filename {:filename (:file diff)}))]
    (e/upconnect! commit-node diff-node "changed" (dissoc diff :file))))

(defn connect-commit-to-parent [commit-node parent-hash]
  (let [parent-node (first (v/upsert! :commit-hash {:commit-hash parent-hash}))]
    (e/upconnect! commit-node parent-node "child-of")))

(defn connect-commit-to-people-mentioned
  [commit-node [key people]]
  (doseq [person people]
    (let [person-node (first (v/upsert! :name (dissoc person :date :timezone)))]
      (e/upconnect! commit-node person-node (name key)))))

(defn load-commit-into-titan [{:keys [people-mentioned diffs message
                                      commit-hash parents author committer] :as commit}]
  (debug-println "INFO: Hash being loaded is " commit-hash)
  (try (g/retry-transact!
        4 (fn [i] (* i i 100))
        (let [author-node    (first (v/upsert! :name (assoc author    :type "person")))
              committer-node (first (v/upsert! :name (assoc committer :type "person")))
              commit-node    (first (v/upsert! :commit-hash {:type "commit"
                                                             :commit-hash commit-hash
                                                             :message message}))]
          (e/upconnect! author-node commit-node "authored")
          (e/upconnect! committer-node commit-node "committed")
          (doseq [diff diffs]
            (connect-commit-to-diff commit-node diff))
          (doseq [parent parents]
            (connect-commit-to-parent commit-node parent))
          (doseq [type-of-mention people-mentioned]
            (connect-commit-to-people-mentioned commit-node type-of-mention))))
       (catch Exception e
         (debug-println "ERROR: Issues with loading " commit-hash e))))

(defn load-tag-into-titan [{:keys [tagger object tag-name date] :as tag}]
  (debug-println "INFO: Tag being loaded is " tag-name)
  (try
    (g/retry-transact!
     4 (fn [i] (* i i 100))
     (let [object-node    (first (v/upsert! :commit-hash object))
           tagger-node    (when (and (:name tagger) (:email tagger))
                            (first (v/upsert! :name tagger)))
           tag-node       (first (v/upsert! :tag-name {:type "tag"
                                                       :tag-name tag-name
                                                       :date date}))]
       (when tagger-node (e/upconnect! tagger-node tag-node "authored"))
       (e/upconnect! tag-node object-node "tagged")))
    (catch Exception e
         (debug-println "ERROR: Issues with loading " tag-name e))))

(defn upload-repo []
  (debug-println "INFO: Starting upload.")
  (titan/start)
  (doall (map (comp load-tag-into-titan git/parse-tag-from-name)
              (git/git-tag-list)))
  
  (doall (map (comp load-commit-into-titan git/parse-commit-from-hash)
              (git/git-rev-list)))   
  (debug-println "INFO: Upload successful. WAHOOOOOOO!")
  (System/exit 0))

;;TODO: Figure out how to update and reemit the xml without pulling my
;;hair out.
(defn update-xml-file [file]
  (let [template-xml (xml-seq (xml/parse-str (slurp "resources/base-rexster.xml")))
        updated-xml  (get-in template-xml [:content] file)
        ]
    (->>  (slurp "resources/base-rexster.xml")
          (spit "resources/rexster.xml"))
    (println "TODO: Make this update automatically to use the correct git-config")
    ))

(defn start-server []
  (debug-println "INFO: Starting rexster server.")
  (update-xml-file storage-dir)
  (.start (Thread.
           (fn []
             (try (com.tinkerpop.rexster.Application/main
                   (into-array String ["--start" "-c" "resources/rexster.xml"]))
                  (catch Exception e (println e)))))))

(defn analyze-repo []
  (debug-println "INFO: Starting analysis of repo.")
  (debug-println "THOUGHT: This should probably do something."))

(defn -main [& args]
  (let [task (case (first args)
               "uploader"  upload-repo
               "server"   start-server ;;TODO This hangs, don't use it yet.
               "analysis" analyze-repo
               (fn []
                 (println "Please provide one of the following commands:")
                 (println "`lein run uploader` uploads the repo into titan.")       
                 (println "`lein run server`   starts a rexster server.")
                 (println "`lein run analysis` runs through all of the scripts in src/bumi/analysis.")))]
    (apply task (rest args))))
