
                  (debug-println "DEUG: Problem parsing the name" line e)))
        filename (-> (first lines)
                     (s/split #" ")
                     first
                     ((partial drop 2))
                     s/join)
    {:filename filename