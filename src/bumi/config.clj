(ns bumi.config)

(def graph-config
  {:storage {:backend "cassandra"
             :hostname "localhost"}})

(def git-root-dir
  (do
    (println (slurp "flag"))
    (case (slurp "flag")
      "laptop" "/Users/zackmaril/Projects/experiments/linux/"
      "amazon" "/home/ec2-user/bumi")))
