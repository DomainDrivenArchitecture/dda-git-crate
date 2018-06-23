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
   [clojure.string :as string]
   [schema.core :as s]
   [pallet.actions :as actions]
   [dda.pallet.commons.secret :as secret]
   [dda.pallet.dda-git-crate.infra :as crate-schema]
   [dda.pallet.dda-git-crate.domain.parse-url :as pu]))

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
                              (s/optional-key :password) secret/Secret}})

(def GitCredentialsResolved (secret/create-resolved-schema GitCredentials))

(s/defn ^:private
  server-trust :- crate-schema/ServerTrust
  [elem]
  (let [{:keys [host port scheme]} elem
        resolved-port (if (some? port)
                          port
                          (cond (= scheme "ssh") 22
                                (= scheme "https") 443))]
    {:pin-fqdn-or-ip {:host (first (string/split host #":")) :port resolved-port}}))

(s/defn ^:private
  git-repository :- GitRepository
  [local-root :- s/Str
   repo-group :- s/Keyword
   credentials :- GitCredentialsResolved
   elem :- s/Any]
  (let [{:keys [host scheme path port user]} elem
        parsed-host (first (string/split host #":"))
        server-type (if (re-matches #"github.com" parsed-host) :github :gitblit)
        transport-type (cond
                         (= scheme "ssh") :ssh
                         (and
                          (some? user)
                          (not (= "git" user))) :https-private
                         :default :https-public)
        current-credentials (server-type credentials)
        path-without-gitblit-r (if (and (= server-type :gitblit)
                                        (= (first path) "r")) (rest path)
                                   path)
        path-without-orga (if (and (= server-type :github)
                                   (not (= transport-type :ssh))) (rest path-without-gitblit-r)
                              path-without-gitblit-r)
        repo (string/join "/" path-without-orga)
        orga-map (cond (and (= server-type :github)
                            (= transport-type :ssh)) {:orga (fnext (string/split host #":"))}
                       (= server-type :github) {:orga (first path-without-gitblit-r)}
                       :default {})
        port-map (if (some? port) {:ssh-port port} {})
        credentials-map (cond (= transport-type :https-private) {:user-credentials current-credentials}
                              (= transport-type :https-public) {:user-credentials {}}
                              (and (= server-type :gitblit)
                                   (= transport-type :ssh)) {:user-credentials {:user (:user current-credentials)}}
                              :default {:user-credentials {:user "git"}})]
    (merge
     {:fqdn parsed-host
      :repo repo
      :local-dir (str local-root (name repo-group) "/"
                      (first (string/split repo #".git")))
      :server-type server-type
      :transport-type transport-type}
     orga-map
     port-map
     credentials-map)))

(s/defn
  git-url :- s/Str
  [repository :- GitRepository]
  (let [{:keys [user-credentials fqdn ssh-port repo local-dir
                transport-type server-type orga]} repository
        cred (cond
               (= :https-public transport-type) ""
               (= :https-private transport-type) (str
                                                   (:user user-credentials) ":"
                                                   (:password user-credentials) "@")
               (= :ssh transport-type) (str (:user user-credentials) "@"))
        protocol (if (= :ssh transport-type)
                   "ssh://" "https://")
        base-path (cond
                    (and
                      (= :gitblit server-type)
                      (not (= :ssh transport-type))) "/r"
                    (and
                      (= :github server-type)
                      (= :ssh transport-type)) (str ":" orga)
                    (and
                      (= :github server-type)
                      (not (= :ssh transport-type))) (str "/" orga)
                    :default "")
        server (if (and
                     (= :ssh transport-type)
                     (contains? repository :ssh-port))
                 (str fqdn ":" ssh-port) fqdn)]
    (str protocol cred server base-path "/" repo)))


(s/defn
  crate-repo :- crate-schema/GitRepository
  [synced :- s/Bool
   domain-repo :- GitRepository]
  {:repo (git-url domain-repo)
   :local-dir (:local-dir domain-repo)
   :settings (if synced
               #{:sync}
               #{})})

(s/defn
  collect-trust :- [crate-schema/ServerTrust]
  [domain-repo-uris :- [s/Str]]
  (let [parsed-uris (map pu/string->url domain-repo-uris)]
    (distinct
      (map server-trust parsed-uris))))

(s/defn
  collect-repo-group :- [crate-schema/GitRepository]
  [credentials :- GitCredentialsResolved
   synced :- s/Bool
   local-root :- s/Str
   key :- s/Keyword
   repo-group :- [s/Str]]
  (let [parsed-uris (map pu/string->url repo-group)]
    (map
     #(crate-repo
       synced
       (git-repository local-root key credentials %))
     parsed-uris)))


(s/defn
  collect-repo :- [crate-schema/GitRepository]
  [credentials :- GitCredentialsResolved
   synced :- s/Bool
   local-root :- s/Str
   domain-repo-uris :- {s/Keyword [s/Str]}]
  (flatten
    (map
     #(collect-repo-group credentials synced local-root (key %) (val %))
     domain-repo-uris)))
