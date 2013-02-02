(ns bumi.core
  (:require [linux-titan.git :as git]))

(def linux-src-dir "/Users/zackmaril/Projects/experiments/linux")
(def rev-list (git/produce-rev-list linux-src-dir))
(def commits (map git/parse-commit-from-hash rev-list))
