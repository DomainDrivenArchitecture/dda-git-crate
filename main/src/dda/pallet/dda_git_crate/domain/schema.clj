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

(ns dda.pallet.dda-git-crate.domain.schema
  (:require
   [schema.core :as s]
   [dda.pallet.dda-git-crate.domain.repo :as repo]))

(def GitRepository repo/GitRepository)

(def GitCredentials repo/GitCredentials)

(def GitDomainConfig
  (s/both
     (s/pred #(or
               (contains? %1 :repo-groups)
               (contains? %1 :repos))
              "Either :repo-groups or :repos has to be present.")
     {:os-user s/Keyword
      :user-email s/Str
      (s/optional-key :signing-key) s/Str
      (s/optional-key :diff-tool) s/Str
      (s/optional-key :credentials) GitCredentials
      (s/optional-key :repo-groups) (hash-set (s/enum :dda-pallet))
      (s/optional-key :repos) {s/Keyword [s/Str]}
      (s/optional-key :synced-repos) {s/Keyword [s/Str]}}))
