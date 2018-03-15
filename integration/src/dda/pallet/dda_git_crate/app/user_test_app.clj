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
(ns dda.pallet.dda-git-crate.app.user-test-app
  (:require
    [schema.core :as s]
    [dda.config.commons.map-utils :as mu]
    [dda.pallet.commons.secret :as secret]
    [dda.pallet.core.app :as core-app]
    [dda.pallet.dda-config-crate.infra :as config-crate]
    [dda.pallet.dda-git-crate.infra :as infra]
    [dda.pallet.dda-git-crate.app :as app]
    [dda.pallet.dda-user-crate.app :as user]
    [dda.pallet.dda-serverspec-crate.app :as serverspec]))

(def GitServerspecUserDomainConfig
  {:git app/GitDomainConfig
   :serverspec serverspec/ServerspecDomainConfig
   :user user/UserDomainConfig})

(def GitServerspecUserDomainConfigResolved
  {:git app/GitDomainConfig
   :serverspec serverspec/ServerspecDomainConfig
   :user user/UserDomainConfigResolved})

(s/defn ^:always-validate
  app-configuration-resolved
  [domain-config :- GitServerspecUserDomainConfigResolved]
  (let [{:keys [git user serverspec]} domain-config]
    (mu/deep-merge
      (user/app-configuration user :group-key :dda-git-group)
      (app/app-configuration git :group-key :dda-git-group)
      (serverspec/app-configuration serverspec :group-key :dda-git-group))))

(s/defn ^:always-validate
  app-configuration
  [domain-config :- GitServerspecUserDomainConfig]
  (let [resolved-domain-config (secret/resolve-secrets domain-config GitServerspecUserDomainConfig)]
    (apply app-configuration-resolved resolved-domain-config)))

(s/defmethod ^:always-validate
  core-app/group-spec infra/facility
  [crate-app
   domain-config :- GitServerspecUserDomainConfig]
  (let [app-config (app-configuration domain-config)]
    (core-app/pallet-group-spec
     app-config [(config-crate/with-config app-config)
                 serverspec/with-serverspec
                 user/with-user
                 app/with-git])))

(def crate-app (core-app/make-dda-crate-app
                  :facility infra/facility
                  :domain-schema GitServerspecUserDomainConfig
                  :domain-schema-resolved GitServerspecUserDomainConfigResolved
                  :default-domain-file "git.edn"))
