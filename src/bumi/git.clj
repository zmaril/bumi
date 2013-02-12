        [bumi.config :only (git-root-dir debug-println)]))
                  (println "DEUG: Problem parsing the name" line e)))
(defn parse-diff [raw]
  (let [lines (s/split-lines raw)
        file (-> (first lines)
     :diff (s/join "\n" (drop-while (complement (partial re-find #"^@@")) lines))}))
                   (debug-println "ERROR: Problem parsing date of " tag-name)))
                          (debug-println "ERROR: Problem finding commit of " tag-name)))]