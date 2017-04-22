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

(ns dda.pallet.domain.dda-git-crate.schema
  (:require
   [schema.core :as s]))

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

(def GitCredentials
  {(s/enum :gitblit :github) {:user s/Str
                              (s/optional-key :password) s/Str}})

(def GitDomainConfig
     {:os-user s/Keyword
      :user-email s/Str
      (s/optional-key :credentials) GitCredentials
      :repo-groups (hash-set (s/enum :dda-pallet))})
