(ns bumi.config)

(def env (into {} (System/getenv)))

(def graph-config
  { ;; Embedded cassandra settings
   "storage.backend"  "embeddedcassandra"
   "storage.cassandra-config-dir" 
   (str "file://" (System/getProperty "user.dir")  "/resources/cassandra.yaml") }  )

(def git-root-dir (or (env "BUMI_GIT_DIR")
                      "/Users/zackmaril/Projects/experiments/linux/"))

(def debug (boolean (env "BUMI_DEBUG")))

(defmacro debug-println [& body]
  `(when ~debug
     (println ~@body)))
