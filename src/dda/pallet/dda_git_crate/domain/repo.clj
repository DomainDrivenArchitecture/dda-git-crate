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
   [clojure.walk :refer (keywordize-keys)]
   [pallet.actions :as actions]
   [pallet.api :as api]
   [schema.core :as s]
   [dda.pallet.dda-git-crate.infra :as crate-schema]
   [dda.pallet.dda-git-crate.domain.git-url :as git-url]
   [dda.pallet.dda-git-crate.domain.schema :as domain-schema]
   [dda.pallet.dda-git-crate.domain.parse-url :as pu]))

(s/defn ^:private server-trust :- crate-schema/ServerTrust
  [elem]
  (let [host (:host elem)]
    {:pin-fqdn-or-ip (first (string/split host #":"))}))

(s/defn ^:private git-repository :- domain-schema/GitRepository
  [local-root :- s/Str
   repo-group :- s/Keyword
   credentials :- domain-schema/GitCredentials
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

(s/defn crate-repo :- crate-schema/GitRepository
  [domain-repo :- domain-schema/GitRepository]
  {:repo (git-url/git-url domain-repo)
   :local-dir (:local-dir domain-repo)})

(s/defn collect-trust :- [crate-schema/ServerTrust]
  [domain-repo-uris :- [s/Str]]
  (let [parsed-uris (map pu/string->url domain-repo-uris)]
    (distinct
      (map server-trust parsed-uris))))

(s/defn collect-repo :- [crate-schema/GitRepository]
  [credentials :- domain-schema/GitCredentials
   local-root :- s/Str
   domain-repo-uris :- {s/Keyword [s/Str]}]
  (let [parsed-uris (map pu/string->url (first (vals domain-repo-uris)))]
    (map
     #(crate-repo
       (git-repository local-root (first (keys domain-repo-uris)) credentials %))
     parsed-uris)))
