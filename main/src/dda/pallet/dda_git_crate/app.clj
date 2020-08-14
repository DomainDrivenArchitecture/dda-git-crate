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

(ns dda.pallet.dda-git-crate.app
  (:require
   [schema.core :as s]
   [dda.pallet.commons.secret :as secret]
   [dda.pallet.core.app :as core-app]
   [dda.pallet.dda-config-crate.infra :as config-crate]
   [dda.pallet.dda-serverspec-crate.app :as serverspec]
   [dda.pallet.dda-git-crate.infra :as infra]
   [dda.pallet.dda-git-crate.convention :as convention]))

(def with-git infra/with-git)

(def InfraResult convention/InfraResult)

(def GitConvention convention/GitConvention)

(def GitConventionResolved (secret/create-resolved-schema GitConvention))

(def GitAppConfig
  {:group-specific-config {s/Keyword InfraResult}})

(s/defn ^:always-validate
  app-configuration-resolved :- GitAppConfig
  [convention-config :- GitConventionResolved
   & options]
  (let [{:keys [group-key] :or {group-key infra/facility}} options]
    {:group-specific-config {group-key (convention/infra-configuration convention-config)}}))

(s/defn ^:always-validate
  app-configuration :- GitAppConfig
  [convention-config :- GitConvention
   & options]
  (let [resolved-convention-config (secret/resolve-secrets convention-config GitConvention)]
    (apply app-configuration-resolved resolved-convention-config options)))

(s/defmethod ^:always-validate
  core-app/group-spec infra/facility
  [crate-app
   convention-config :- GitConventionResolved]
  (let [app-config (app-configuration-resolved convention-config)]
    (core-app/pallet-group-spec
      app-config [(config-crate/with-config app-config)
                  serverspec/with-serverspec
                  with-git])))

(def crate-app (core-app/make-dda-crate-app
                  :facility infra/facility
                  :convention-schema GitConvention
                  :convention-schema-resolved GitConventionResolved
                  :default-convention-file "git.edn"))
