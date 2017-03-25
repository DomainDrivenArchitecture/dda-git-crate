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
(ns dda.pallet.crate.dda-git-crate.schema
  (:require
    [clojure.tools.logging :as logging]
    [schema.core :as s]
    [pallet.api :as api]
    [pallet.actions :as actions]
    [pallet.crate :as crate]
    [pallet.crate.git :as git]
    [org.domaindrivenarchitecture.pallet.core.dda-crate :as dda-crate]
    [org.domaindrivenarchitecture.pallet.servertest.fact.packages :as package-fact]
    [org.domaindrivenarchitecture.pallet.servertest.test.packages :as package-test]))

(def GitRepository
  {:user-credentials {(s/optional-key :user) s/Str
                      (s/optional-key :password) s/Str}
   :fqdn s/Str
   (s/optional-key :ssh-port) s/Str
   (s/optional-key :orga) s/Str
   :repo s/Str
   :local-dir s/Str
   :transport-type (s/enum :ssh :https-public :https-private)
   :server-type (s/enum :gitblit :github)})

(def GitCrateConfig
  {s/Keyword [GitRepository]})
