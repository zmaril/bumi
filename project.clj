(defproject bumi "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [hermes/hermes "0.2.2"]
                 [me.raynes/conch "0.5.0"]
                 [com.tinkerpop.blueprints/blueprints-core "2.2.0"]                 
                 [com.tinkerpop.rexster/rexster-server "2.2.0"]
                 [org.clojure/data.xml "0.0.7"]]
  :jvm-opts ["-Xms1g" "-Xmx2g" "-server"]
  :main bumi.core)