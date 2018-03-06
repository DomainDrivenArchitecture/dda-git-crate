; Licensed to the Apache Software Foundation (ASF) under one
; or more contributor license agreements. See the NOTICE file
; distributed with this work for additional information
; regarding copyright ownership. The ASF licenses this file
; to you under the Apache License, Version 2.0 (the
; "License"); you may not use this file except in compliance
; with the License. You may obtain a copy of the License at
;
; http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.
(ns dda.pallet.dda-git-crate.infra
  (:require
    [clojure.tools.logging :as logging]
    [schema.core :as s]
    [pallet.api :as api]
    [pallet.actions :as actions]
    [pallet.crate :as crate]
    [pallet.crate.git :as git]
    [dda.pallet.core.dda-crate :as dda-crate]
    [dda.pallet.dda-git-crate.infra.schema :as git-schema]
    [dda.pallet.dda-git-crate.infra.git-repo :as git-repo]
    [dda.pallet.dda-git-crate.infra.git-config :as git-config]
    [dda.pallet.dda-git-crate.infra.server-trust :as server-trust]))

(def facility :dda-git)
(def version  [0 1 0])

(def ServerTrust
  git-schema/ServerTrust)

(def GitRepository
  git-schema/GitRepository)

(def GitConfig
  git-schema/GitConfig)

(s/defmethod dda-crate/dda-settings facility
  [dda-crate config])
                                        ;(package-fact/collect-packages-fact)

(s/defn configure-user
  "configure user setup"
  [config :- GitConfig]
  (doseq [user (keys config)]
    (let [user-config (user config)
          user-name (name user)
          {:keys [email repo trust]} user-config]
      (pallet.action/with-action-options
        {:sudo-user user-name
         :script-env {:HOME (str "/home/" user-name "/")}}
        (git-config/configure-user user-name email)
        (doseq [trust-element trust]
          (when (contains? trust-element :pin-fqdn-or-ip)
            (server-trust/add-node-to-known-hosts (:pin-fqdn-or-ip trust-element)))
          (when (contains? trust-element :fingerprint)
            (server-trust/add-fingerprint-to-known-hosts (:fingerprint trust-element))))
        (doseq [repo-element repo]
          (let [repo-parent (git-repo/project-parent-path repo-element)]
            (git-repo/create-project-parent repo-parent)
            (git-repo/clone repo-element)))))))

(s/defmethod dda-crate/dda-configure facility
  [dda-crate config]
  "dda-git: configure"
  (configure-user config))

(s/defmethod dda-crate/dda-install facility
  [dda-crate config]
  "dda-git: install routine"
  (git/install))

(s/defmethod dda-crate/dda-test facility
  [dda-crate partial-effective-config])

(def dda-git-crate
  (dda-crate/make-dda-crate
   :facility facility
   :version version))

(def with-git
  (dda-crate/create-server-spec dda-git-crate))