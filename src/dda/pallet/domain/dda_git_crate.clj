; Copyright (c) meissa GmbH. All rights reserved.
; You must not remove this notice, or any other, from this software.

(ns dda.pallet.domain.dda-git-crate
  (:require
    [pallet.actions :as actions]
    [pallet.api :as api]    
    [schema.core :as s]
    [org.domaindrivenarchitecture.config.commons.map-utils :as map-utils]
    [org.domaindrivenarchitecture.pallet.core.dda-crate :as dda-crate]
    [org.domaindrivenarchitecture.pallet.crate.config :as config-crate]
    [dda.pallet.crate.dda-git-crate :as git-crate]
    ))

(def dda-git-domain-config 
  {})


(def dda-git-crate-stack-config
  {:ssh-keys s/Any
   :os-user s/Any
   :group-specific-config 
   {:dda-git-group 
    {:host-name s/Str 
     :domain-name s/Str
     :additional-config {:dda-git nil}}}
   }
  )

(s/defn ^:always-validate dda-git-crate-stack-configuration :- dda-git-crate-stack-config
  [convention-config :- dda-git-domain-config]
  {:ssh-keys nil
   :os-user nil
   :group-specific-config 
   {:dda-git-group 
    {:host-name nil
     :domain-name nil
     :additional-config {:dda-git nil}}}
   }
  )


(s/defn ^:always-validate dda-git-group
  [domain-config :- dda-git-domain-config]
  (let [target (get-in domain-config [:target])
        config (dda-git-crate-stack-configuration domain-config)]
    (api/group-spec
      "dda-git-group"
      :extends [(config-crate/with-config config)
                git-crate/with-git]
      :node-spec (get-in target [:aws :aws-node-spec])
      :count (get-in target [:aws :count])))
  )
