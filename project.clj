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
                 [clojurewerkz/titanium "1.0.0-beta1"]
                 ;;Pallet stuff
                 [org.cloudhoist/pallet "0.7.4"]
                 [org.cloudhoist/pallet-jclouds "1.5.2"]
                 ;; To get started we include all jclouds compute providers.
                 ;; You may wish to replace this with the specific jclouds
                 ;; providers you use, to reduce dependency sizes.
                 [org.jclouds/jclouds-allblobstore "1.5.5"]
                 [org.jclouds/jclouds-allcompute "1.5.5"]
                 [org.jclouds.driver/jclouds-slf4j "1.5.5"
                  ;; the declared version is old and can overrule the
                  ;; resolved version
                  :exclusions [org.slf4j/slf4j-api]]
                 [org.jclouds.driver/jclouds-sshj "1.5.5"]
                 [ch.qos.logback/logback-classic "1.0.9"]]
  :local-repo-classpath true
  :dev-dependencies [[org.cloudhoist/pallet
                      "0.7.4" :type "test-jar"]
                     [org.cloudhoist/pallet-lein "0.5.2"]]
  :profiles {:dev
             {:dependencies
              [[org.cloudhoist/pallet "0.7.4"
                :classifier "tests"]]
              :plugins
              [[org.cloudhoist/pallet-lein "0.5.2"]]}
             :leiningen/reply
             {:dependencies [[org.slf4j/jcl-over-slf4j "1.7.2"]]
              :exclusions [commons-logging]}}
  :jvm-opts ["-Xmx10g" "-server"]
  :main bumi.core)