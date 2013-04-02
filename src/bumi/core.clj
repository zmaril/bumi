(ns bumi.core
  (:require [bumi.git :refer (RevCommit->map rev-list)]))

(defn -main [& args]
  (map RevCommit->map rev-list))