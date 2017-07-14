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
    [pallet.api :as api]
    [dda.config.commons.map-utils :as mu]
    [dda.pallet.dda-config-crate.infra :as config-crate]
    [dda.pallet.dda-git-crate.infra :as infra]
    [dda.pallet.dda-git-crate.domain :as domain]
    [dda.pallet.dda-git-crate.app :as app]
    [dda.pallet.dda-user-crate.app :as user]
    [dda.pallet.domain.dda-servertest-crate :as server-test-domain]
    [dda.pallet.crate.dda-servertest-crate :as server-test-crate]))

(defn app-configuration [git-config user-config test-config]
  (mu/deep-merge
    (user/crate-stack-configuration user-config :group-key :dda-git-group)
    (app/app-configuration git-config :group-key :dda-git-group)
    (server-test-domain/crate-stack-configuration test-config :group-key :dda-git-group)))

(defn group-spec [app-config]
 (let []
   (api/group-spec
     "dda-git-group"
     :extends [(config-crate/with-config app-config)
               server-test-crate/with-servertest
               user/with-user
               app/with-git])))
