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
(ns dda.pallet.dda-git-crate.app-2-0
  {:deprecated "2.0"}
  (:require
   [schema.core :as s]
   [dda.pallet.commons.secret :as secret]
   [dda.pallet.core.app :as core-app]
   [dda.pallet.dda-config-crate.infra :as config-crate]
   [dda.pallet.dda-git-crate.infra :as infra]
   [dda.pallet.dda-git-crate.domain-2-0 :as domain]))

(def with-git infra/with-git)

(def InfraResult domain/InfraResult)

(def GitDomainConfig domain/GitDomainConfig)

(def GitDomainConfigResolved (secret/create-resolved-schema GitDomainConfig))

(def GitAppConfig
  {:group-specific-config {s/Keyword InfraResult}})

(s/defn ^:always-validate
  app-configuration-resolved :- GitAppConfig
  [domain-config :- GitDomainConfigResolved
   & options]
  (let [{:keys [group-key] :or {group-key infra/facility}} options]
    {:group-specific-config {group-key (domain/infra-configuration domain-config)}}))

(s/defn ^:always-validate
  app-configuration :- GitAppConfig
  [domain-config :- GitDomainConfig
   & options]
  (let [resolved-domain-config (secret/resolve-secrets domain-config GitDomainConfig)]
    (apply app-configuration-resolved resolved-domain-config options)))

(s/defmethod ^:always-validate
  core-app/group-spec infra/facility
  [crate-app
   domain-config :- GitDomainConfigResolved]
  (let [app-config (app-configuration domain-config)]
    (core-app/pallet-group-spec
      app-config [(config-crate/with-config app-config)
                  with-git])))

(def crate-app (core-app/make-dda-crate-app
                  :facility infra/facility
                  :domain-schema GitDomainConfig
                  :domain-schema-resolved GitDomainConfigResolved
                  :default-domain-file "git.edn"))
