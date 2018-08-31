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
    [clojure.string :as st]
    [pallet.api :as api]
    [pallet.actions :as actions]
    [pallet.crate :as crate]
    [dda.pallet.core.infra :as core-infra]
    [dda.pallet.dda-git-crate.infra.git-repo :as git-repo]
    [dda.pallet.dda-git-crate.infra.git-config :as git-config]
    [dda.pallet.dda-git-crate.infra.server-trust :as server-trust]))

(def facility :dda-git)

(def ServerTrust server-trust/ServerTrust)

(def Repository
  git-repo/Repository)

(def Config
  {:config git-config/Config
   :file-fact-keyword s/Keyword
   :trust [server-trust/ServerTrust]
   :repo [Repository]})

(def GitInfra
  {s/Keyword      ;Keyword is user-name
   Config})

(s/defn configure-user
  "configure user setup"
  [config :- GitInfra]
  (doseq [user (keys config)]
    (let [user-config (user config)
          user-name (name user)
          {:keys [config repo trust file-fact-keyword]} user-config]
      (pallet.action/with-action-options
        {:sudo-user "root"}
        (git-config/configure-user user-name config)
        (server-trust/configure-user facility user-name trust)
        (git-repo/configure-user facility user-name repo file-fact-keyword)))))

(s/defmethod core-infra/dda-configure facility
  [core-infra config]
  "dda-git: configure"
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
