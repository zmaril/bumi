(ns bumi.pallet
  "Node defintions for quickstart"
  (:use
   [pallet.core :only [group-spec server-spec node-spec converge]]
   [pallet.configure :only [compute-service]]
   [pallet.action.package :only [package package-manager]]
   [pallet.action.exec-script :only [exec-script*]]
   [pallet.crate.automated-admin-user :only [automated-admin-user]]
   [pallet.phase :only [phase-fn]]))

(def default-node-spec
  (node-spec
   :image {:os-family :ubuntu :image-id "us-east-1/ami-3c994355"}
   :hardware {:min-cores 12}
   :compute (compute-service :aws)))

(def
  ^{:doc "Defines the type of node quickstart will run on"}
  base-server
  (server-spec
   :phases
   {:bootstrap (phase-fn (automated-admin-user))}))

(def
  ^{:doc "Define a server spec for quickstart"}
  bumi-server
  (server-spec
   :phases
   {:configure (phase-fn
                (package-manager :update)
                (package "git")
                (package "wget")
                (package "openjdk-7-jre-headless")
;;                (exec-script* "sudo wget https://raw.github.com/technomancy/leiningen/stable/bin/lein -O /usr/bin/lein")
;;                (exec-script* "sudo chmod 755 /usr/bin/lein")
;;                (exec-script* "lein")
;;                (exec-script* "git clone https://github.com/mirrors/linux-2.6.git")
;;                (exec-script* "git clone https://github.com/zmaril/bumi.git")
;;                (exec-script* "cd bumi; lein run load")                
                )}))
(def
  ^{:doc "Defines a group spec that can be passed to converge or lift."}
  bumi-group
  (group-spec
   "bumi"
   :extends [base-server bumi-server]
   :node-spec default-node-spec))

(defn init-server 
  "Creates a server loaded up to use with bumi."
  []
  (println "Provisioning server.")
  (let [result (converge (assoc bumi-group :count 1)
                         :compute (compute-service :aws))
        ip (.primary-ip (first (:selected-nodes result)))]
    (println (:results result))
    (println "Use the following to log into your brand new bumi server:")
    (println "ssh" ip)))

(defn kill-server 
  "Creates a server loaded up to use with bumi."
  []
  (println "Killing server.")
  (converge (assoc bumi-group :count 0)
            :compute (compute-service :aws))
  (println "Killed server."))