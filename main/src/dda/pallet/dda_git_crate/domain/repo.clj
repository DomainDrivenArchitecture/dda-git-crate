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
    [clojure.tools.logging :as logging]
    [schema.core :as s]
    [dda.pallet.commons.secret :as secret]
    [dda.config.commons.user-home :as user-home]))

(def ServerIdentity
  {:host s/Str                                 ;identifyer for repo matching
   (s/optional-key :port) s/Num                ;identifyer for repo matching, defaults to 22 or 443 based on protocol
   :protocol (s/enum :ssh :https)})

(def Repository
  (merge
    ServerIdentity
    {(s/optional-key :orga-path) s/Str
     :repo-name s/Str
     :server-type (s/enum :gitblit :github :gitlab)}))

(def OrganizedRepositories {s/Keyword [Repository]})

(def GitCredential
  (merge
     ServerIdentity
     {:user-name secret/Secret                     ;needed for none-public access
      (s/optional-key :password) secret/Secret}))  ;needed for none-public & none-key access

(def GitCredentialResolved (secret/create-resolved-schema GitCredential))

(def GitCredentials [GitCredential])

(def GitCredentialsResolved (secret/create-resolved-schema GitCredentials))

(s/defn
  server-identity-port
  [server-identity]
  (let [{:keys [port protocol]} server-identity]
    (cond
      (contains? server-identity :port) port
      (= protocol :ssh) 22
      (= protocol :https) 443)))

(s/defn
  server-identity-key
  [server-identity]
  (let [{:keys [host]} server-identity]
    (keyword (str host "_" (server-identity-port server-identity)))))

(s/defn
  reduce-trust-map
  [trust-map
   ordinal
   server-identity]
  (let [host (:host server-identity)
        port (server-identity-port server-identity)]
    (merge
      trust-map
      {(server-identity-key server-identity)
       {:host host :port port}})))

(s/defn
  trust
  [repos :- [Repository]]
  (into
    []
    (map
      (fn [v] {:pin-fqdn-or-ip v})
      (vals (reduce-kv reduce-trust-map {} repos)))))

(s/defn server-url
  [credential :- GitCredential
   repo :- Repository]
  (let [{:keys [host protocol]} repo]
    (str (name protocol) "://"
         (when (some? credential)
           (str (:user-name credential)
                (some->> (:password credential) (str ":"))
                "@"))
         host ":" (server-identity-port repo))))

(s/defn github-url
  [credential :- GitCredential
   repo :- Repository]
  (let [{:keys [host orga-path repo-name protocol server-type]} repo]
    (str (server-url credential repo)
      "/" orga-path "/" repo-name ".git")))

(s/defn gitblit-url
  [credential :- GitCredentialResolved
   repo :- Repository]
  (let [{:keys [host orga-path repo-name protocol server-type]} repo]
    (str (server-url credential repo)
      "/r/" orga-path "/" repo-name ".git")))

(s/defn infra-repo
  [user :- s/Keyword
   is-synced? :- s/Bool
   orga-group :- s/Keyword
   credentials :- GitCredentialsResolved
   repo :- Repository]
  (let [{:keys [host port orga-path repo-name protocol server-type]} repo
        credential (get credentials (server-identity-key repo))]
    {:repo
     (cond (= :github server-type) (github-url credential repo)
           (= :gitblit server-type) (gitblit-url credential repo))
     :local-dir
     (str (user-home/user-home-dir (name user))
          "/repos/"
          (name orga-group)
          "/"
          repo-name)
     :settings
      (if is-synced?
        #{:sync}
        #{})}))

(s/defn infra-repos
  [user :- s/Keyword
   is-synced? :- s/Bool
   credentials :- GitCredentialsResolved
   repos :- OrganizedRepositories]
  (reduce-kv
    (fn [col k v]
      (into
        col
        (map
          #(infra-repo user is-synced? k credentials %)
          v)))
    []
    repos))
