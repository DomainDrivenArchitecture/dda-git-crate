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
(ns dda.pallet.crate.dda-git-crate
  (:require
    [clojure.tools.logging :as logging]
    [schema.core :as s]
    [pallet.api :as api]
    [pallet.actions :as actions]
    [pallet.crate :as crate]
    [pallet.crate.git :as git]
    [org.domaindrivenarchitecture.pallet.core.dda-crate :as dda-crate]
    [dda.pallet.crate.dda-git-crate.schema :as git-schema]
    [dda.pallet.crate.dda-git-crate.git-repo :as git-repo]
    [dda.pallet.crate.dda-git-crate.server-trust :as server-trust]
    [org.domaindrivenarchitecture.pallet.servertest.fact.packages :as package-fact]
    [org.domaindrivenarchitecture.pallet.servertest.test.packages :as package-test]))

(def facility :dda-git)
(def version  [0 1 0])

(def GitRepository
  git-schema/GitRepository)

(def GitConfig
  git-schema/GitConfig)

(s/defmethod dda-crate/dda-settings facility
  [dda-crate partial-effective-config])
  ;(package-fact/collect-packages-fact)

(s/defn configure-user
  "configure user setup"
  [config :- GitConfig]
  (let [user :ubuntu
        user-name (name user)
        repos (get-in config [user :repo])
        trusts (get-in config [user :trust])]
    (pallet.action/with-action-options
        {:sudo-user user-name
         :script-env {:HOME (str "/home/" user-name "/")}}
      (doseq [trust trusts]
        (when (contains? trust :pin-fqdn-or-ip)
          (server-trust/add-node-to-known-hosts (:pin-fqdn-or-ip trust)))
        (when (contains? trust :fingerprint)
          (server-trust/add-fingerprint-to-known-hosts (:fingerprint trust))))
      (doseq [repo repos]
        (let [repo-parent (git-repo/project-parent-path repo)]
          (git-repo/create-project-parent repo-parent)
          (git-repo/clone repo))))))

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
