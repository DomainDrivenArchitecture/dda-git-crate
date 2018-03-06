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
(ns dda.pallet.dda-git-crate.app.user-app
  (:require
    [schema.core :as s]
    [dda.cm.group :as group]
    [dda.config.commons.map-utils :as mu]
    [dda.pallet.commons.secret :as secret]
    [dda.pallet.core.app :as core-app]
    [dda.pallet.dda-config-crate.infra :as config-crate]
    [dda.pallet.dda-git-crate.infra :as infra]
    [dda.pallet.dda-git-crate.app :as app]
    [dda.pallet.dda-user-crate.app :as user]))

(def GitUserDomainConfig
  {:git app/GitDomainConfig
   :user user/UserDomainConfig})

(def GitUserDomainConfigResolved
  {:git app/GitDomainConfig
   :user user/UserDomainConfigResolved})

(s/defn ^:always-validate
  app-configuration-resolved
  [domain-config :- GitUserDomainConfigResolved]
  (let [{:keys [git user]} domain-config]
    (mu/deep-merge
      (user/app-configuration user :group-key :dda-git-group)
      (app/app-configuration git :group-key :dda-git-group))))

(s/defn ^:always-validate
  app-configuration
  [domain-config :- GitUserDomainConfig]
  (let [resolved-domain-config (secret/resolve-secrets domain-config GitUserDomainConfig)]
    (apply app-configuration-resolved resolved-domain-config)))

(s/defmethod ^:always-validate
  core-app/group-spec infra/facility
  [crate-app
   domain-config :- GitUserDomainConfig]
  (let [app-config (app-configuration domain-config)]
    (group/group-spec
     app-config [(config-crate/with-config app-config)
                 user/with-user
                 app/with-git])))

(def crate-app (core-app/make-dda-crate-app
                  :facility infra/facility
                  :domain-schema GitUserDomainConfig
                  :domain-schema-resolved GitUserDomainConfigResolved
                  :default-domain-file "git.edn"))
