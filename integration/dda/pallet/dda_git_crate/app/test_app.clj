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
    [dda.pallet.dda-config-crate.infra :as config-crate]
    [dda.pallet.dda-git-crate.app :as app]
    [dda.pallet.dda-user-crate.app :as user]
    [dda.pallet.dda-servertest-crate.app :as test]))

(defn app-configuration [git-config test-config]
  (mu/deep-merge
   (app/app-configuration git-config)
   (test/app-configuration test-config :group-key :dda-git-group)))

(s/defn ^:always-validate git-group-spec
  [app-config :- app/GitAppConfig]
  (group/group-spec
   app-config [(config-crate/with-config app-config)
               test/with-servertest
               app/with-git]))
