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

(ns dda.pallet.dda-git-crate.domain.repo
  (:require
   [schema.core :as s]))

(def ServerIdentity
  {:host s/Str                                 ;identifyer for repo matching
   (s/optional-key :port) s/Num                ;identifyer for repo matching, defaults to 22 or 443 based on access-type
   :access-type (s/enum :ssh :https)})

(def GitRepository
  (merge
    ServerIdentity
    {(s/optional-key :orga-path) s/Str
     :repo-name s/Str
     :server-type (s/enum :gitblit :github :gitlab)}))

(def GitCredentials
  [(merge
     ServerIdentity
     {(s/optional-key :user-name) secret/Secret    ;needed for none-public access
      (s/optional-key :password) secret/Secret})]) ;needed for none-public & none-key access

(s/defn
  server-identity-port
  [server-identity]
  (let [{:keys [port access-type]} server-identity]
    (cond
      (contains? server-identity :port) port
      (= access-type :ssh) 22
      (= access-type :https) 443)))

(s/defn
  trust
  [repos :- [GitRepository]]
  (reduce-kv
    (fn [m k v]
      (merge
        m
        {(str (:host v) "_" (server-identity-port v))
         {:host (:host v) :port (server-identity-port v)}})))
  {}
  repos)
