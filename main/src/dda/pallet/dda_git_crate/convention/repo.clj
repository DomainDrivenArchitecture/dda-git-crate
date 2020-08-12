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

(ns dda.pallet.dda-git-crate.convention.repo
  (:require
    [clojure.string :as st]
    [clojure.tools.logging :as logging]
    [schema.core :as s]
    [dda.pallet.commons.secret :as secret]
    [dda.config.commons.user-home :as user-home]
    [dda.pallet.dda-git-crate.infra.git-repo :as repo]))

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
  [credential ;:- GitCredential - schema is not able to validate
   repo :- Repository]
  (let [{:keys [host protocol]} repo]
    (str (name protocol) "://"
         (when (some? credential)
           (str (:user-name credential)
                (when (and
                        (= :https protocol)
                        (some? (:password credential)))
                      (str ":" (:password credential)))
                "@"))
         host ":" (server-identity-port repo))))

(s/defn github-url
  [credential ; :- GitCredential - schema is unable to validate
   repo :- Repository]
  (let [{:keys [host orga-path repo-name protocol server-type]} repo]
    (cond (= :https protocol)
          (str (server-url credential repo) "/"
               orga-path "/" repo-name ".git")
          (= :ssh protocol)
          (str "git@github.com:"
               orga-path "/" repo-name ".git"))))

(s/defn gitblit-url
  [credential ; :- GitCredentialResolved - schema is unable to validate
   repo :- Repository]
  (let [{:keys [host orga-path repo-name protocol server-type]} repo]
    (str (server-url credential repo)
      "/"
      (when (= :https protocol) "r/")
      orga-path "/" repo-name ".git")))

(s/defn gitlab-url
  [credential ; :- GitCredential - schema is not able to validate
   repo :- Repository]
  (let [{:keys [host orga-path repo-name protocol server-type]} repo]
    (cond (= :https protocol)
          (str (server-url credential repo) "/"
               orga-path "/" repo-name ".git")
          (= :ssh protocol)
          (str "git@" host ":"
               orga-path "/" repo-name ".git"))))

(s/defn credential-map
  [credentials] ; :- GitCredentialsResolved] - schema is unable to validate
  (reduce-kv
    (fn [col k v]
      (merge col
             {(server-identity-key v)
              (select-keys v [:user-name :password])}))
    {}
    credentials))

(s/defn repo-directory-name
  [user :- s/Keyword
   orga-group :- s/Keyword
   repo :- Repository]
  (let [{:keys [repo-name]} repo]
     (str (user-home/user-home-dir (name user))
          "/repo/"
          (name orga-group)
          "/"
          repo-name)))

(s/defn infra-repo
  [user :- s/Keyword
   is-synced? :- s/Bool
   orga-group :- s/Keyword
   credentials ; :- GitCredentialsResolved - schema is not able to validate
   repo :- Repository]
  (let [{:keys [host port orga-path repo-name protocol server-type]} repo
        credential (get credentials (server-identity-key repo))]
    {:repo
     (cond (= :github server-type) (github-url credential repo)
           (= :gitblit server-type) (gitblit-url credential repo)
           (= :gitlab server-type) (gitlab-url credential repo))
     :local-dir
     (repo-directory-name user orga-group repo)
     :settings
     (if is-synced?
       #{:sync}
       #{})}))

(s/defn infra-fact
  [user :- s/Keyword
   orga-group :- s/Keyword
   repo :- Repository]
  (let [dir (repo-directory-name user orga-group repo)]
    {(repo/path-to-keyword dir) {:path dir}}))

(s/defn infra-repos
  [user :- s/Keyword
   is-synced? :- s/Bool
   credentials ; :- GitCredentialsResolved - schema is unable to validate
   repos] ; :- OrganizedRepositories - schema is unable to validate]
  (reduce-kv
    (fn [col k v]
      (into
        col
        (map
          #(infra-repo user is-synced? k (credential-map credentials) %)
          v)))
    []
    repos))

(s/defn infra-facts
  [user :- s/Keyword
   repos] ; :- OrganizedRepositories] - schema is unable to validate
  (reduce-kv
    (fn [col k v]
      (apply merge
        col
        (map
          #(infra-fact user k %)
          v)))
    {}
    repos))
