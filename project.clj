(defproject bumi "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories [["typesafe" "http://repo.typesafe.com/typesafe/snapshots/"]
                 ["apache" "http://repository.apache.org/content/repositories/releases/"]
                 ["sonatype" "https://oss.sonatype.org/content/repositories/snapshots/"]
                 ["oracle" "http://download.oracle.com/maven/"]]  
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [clj-jgit "0.3.1"]
                 [clojurewerkz/titanium "1.0.0-alpha4-SNAPSHOT"]]
  :jvm-opts ["-Xms1g" "-Xmx2g" "-server"]
  :main bumi.core)