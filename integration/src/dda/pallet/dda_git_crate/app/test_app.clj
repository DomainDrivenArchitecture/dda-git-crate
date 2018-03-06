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
(ns dda.pallet.dda-git-crate.app.test-app
  (:require
    [schema.core :as s]
    [dda.cm.group :as group]
    [dda.config.commons.map-utils :as mu]
    [dda.pallet.core.app :as core-app]
    [dda.pallet.dda-config-crate.infra :as config-crate]
    [dda.pallet.dda-git-crate.infra :as infra]
    [dda.pallet.dda-git-crate.app :as app]
    [dda.pallet.dda-serverspec-crate.app :as serverspec]))

(def GitServerspecDomainConfig
  {:git app/GitDomainConfig
   :serverspec serverspec/ServerSpecDomainConfig})

(s/defn ^:always-validate
  app-configuration
  [domain-config :- GitServerspecDomainConfig]
  (let [{:keys [git serverspec]} domain-config]
    (mu/deep-merge
     (app/app-configuration git)
     (serverspec/app-configuration serverspec :group-key :dda-git-group))))

(s/defmethod ^:always-validate
  core-app/group-spec infra/facility
  [crate-app
   domain-config :- GitDomainConfig]
  (let [app-config (app-configuration domain-config)]
    (group/group-spec
      app-config [(config-crate/with-config app-config)
                  serverspec/with-serverspec
                  with-git])))

(def crate-app (core-app/make-dda-crate-app
                  :facility infra/facility
                  :domain-schema GitServerspecDomainConfig
                  :domain-schema-resolved GitServerspecDomainConfig
                  :default-domain-file "git.edn"))
