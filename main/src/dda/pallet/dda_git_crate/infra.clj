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
    [dda.pallet.core.infra :as core-infra]
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

(s/defmethod core-infra/dda-settings facility
  [core-infra config])
  ;(package-fact/collect-packages-fact)

(s/defn configure-system
  "configure the system setup"
  [config :- GitConfig]
  (doseq [user (keys config)]
    (let [user-config (user config)
          user-name (name user)
          {:keys [repo ]} user-config]
      (doseq [repo-element repo]
        (git-repo/configure-git-sync user-name repo-element)))))

(s/defn configure-user
  "configure user setup"
  [config :- GitConfig]
  (doseq [user (keys config)]
    (let [user-config (user config)
          user-name (name user)
          {:keys [config repo trust]} user-config]
      (pallet.action/with-action-options
        {:sudo-user "root"}
        (git-config/configure-user user-name config)
        (doseq [trust-element trust]
          (when (contains? trust-element :pin-fqdn-or-ip)
            (server-trust/pin-fqdn-or-ip
              user-name (:pin-fqdn-or-ip trust-element)))
          (when (contains? trust-element :fingerprint)
            (server-trust/add-fingerprint-to-known-hosts
              user-name (:fingerprint trust-element))))
        (doseq [repo-element repo]
          (let [repo-parent (git-repo/project-parent-path repo-element)]
            (git-repo/create-project-parent user-name repo-parent)
            (git-repo/clone user-name repo-element)))))))

(s/defmethod core-infra/dda-configure facility
  [core-infra config]
  "dda-git: configure"
  (configure-system config)
  (configure-user config))

(s/defmethod core-infra/dda-install facility
  [core-infra config]
  "dda-git: install routine"
  (actions/packages :aptitude ["git-core" "git-email"]))

(s/defmethod core-infra/dda-test facility
  [core-infra partial-effective-config])

(def dda-git-crate
  (core-infra/make-dda-crate-infra
   :facility facility))

(def with-git
  (core-infra/create-infra-plan dda-git-crate))
