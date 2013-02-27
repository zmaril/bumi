(ns bumi.config)

(def env (into {} (System/getenv)))

(def storage-dir (or (env "BUMI_STORAGE_DIR")
                            "/tmp/bumigraph/"))
(def graph-config
  {:storage {:backend "cassandra"
             :hostname "localhost"}})

(def git-root-dir (or (env "BUMI_GIT_DIR")
                      "/Users/zackmaril/Projects/experiments/linux/"))

(def debug (boolean (env "BUMI_DEBUG")))

(defmacro debug-println [& body]
  `(when ~debug
     (println ~@body)))
