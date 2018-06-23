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
(ns dda.pallet.dda-git-crate.infra.schema
  (:require
   [schema.core :as s]
   [dda.pallet.dda-git-crate.infra.server-trust :as server-trust]
   [dda.pallet.dda-git-crate.infra.git-config :as config]
   [dda.pallet.dda-git-crate.infra.git-repo :as repo]))

(def ServerTrust
  {(s/optional-key :pin-fqdn-or-ip) server-trust/PinElement
   (s/optional-key :fingerprint) s/Str})

(def GitRepository repo/GitRepository)

(def UserGlobalConfig config/UserGlobalConfig)

(def UserGitConfig
  {:config UserGlobalConfig
   :trust [ServerTrust]
   :repo [GitRepository]})

(def GitConfig
  {s/Keyword      ; Keyword is user-name
   UserGitConfig})
