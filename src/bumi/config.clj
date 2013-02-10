(ns bumi.config)

(def graph-config
  {:storage {:backend "cassandra"
             :hostname "localhost"}})

(def git-root-dir
  (case (slurp "flag")
    "laptop" "/Users/zackmaril/Projects/experiments/linux/"
    "amazon" "/home/ec2-user/bumi"))
