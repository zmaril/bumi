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
                 [hermes/hermes "0.2.2"]
                 [me.raynes/conch "0.5.0"]
                 [com.tinkerpop.blueprints/blueprints-core "2.2.0"]                 
                 [com.tinkerpop.rexster/rexster-server "2.2.0"]
                 [com.thinkaurelius.faunus/faunus "0.2.0-SNAPSHOT"]
                 [org.clojure/data.xml "0.0.7"]]
  :jvm-opts ["-Xms1g" "-Xmx2g" "-server"]
  :main bumi.core)