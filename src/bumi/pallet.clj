(ns bumi.pallet
  "Node defintions for quickstart"
  (:use
   [pallet.core :only [group-spec server-spec node-spec converge]]
   [pallet.configure :only [compute-service]]
   [pallet.action.package :only [package]]
   [pallet.crate.automated-admin-user :only [automated-admin-user]]
   [pallet.phase :only [phase-fn]]))

(def default-node-spec
  (node-spec
   :image {:os-family :ubuntu}
   :hardware {:min-cores 1}
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
                (package "git"))}))
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
  (let [result (converge (assoc bumi-group :count 1)
                         :compute (compute-service :aws))
        ip (.primary-ip (first (:selected-nodes result)))]
    (println "Use the following to log into your brand new bumi server:")
    (println "ssh" ip)))

(defn kill-server 
  "Creates a server loaded up to use with bumi."
  []
  (converge (assoc bumi-group :count 0)
            :compute (compute-service :aws)))