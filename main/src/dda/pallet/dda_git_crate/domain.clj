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

(ns dda.pallet.dda-git-crate.domain
  (:require
   [schema.core :as s]
   [dda.config.commons.map-utils :as mu]
   [dda.config.commons.user-home :as user-home]
   [dda.pallet.commons.secret :as secret]
   [dda.pallet.dda-git-crate.infra :as infra]
   [dda.pallet.dda-serverspec-crate.domain :as spec-domain]
   [dda.pallet.dda-git-crate.domain.repo :as repo]))

(def ServerIdentity repo/ServerIdentity)
(def Repository repo/Repository)
(def OrganizedRepositories repo/OrganizedRepositories)
(def GitCredential repo/GitCredential)
(def GitCredentialResolved repo/GitCredentialResolved)
(def GitCredentials repo/GitCredentials)
(def GitCredentialsResolved repo/GitCredentialsResolved)

(def UserGit
  {:user-email s/Str
   (s/optional-key :signing-key) s/Str
   (s/optional-key :diff-tool) s/Str
   (s/optional-key :credential) repo/GitCredentials
   (s/optional-key :repo) repo/OrganizedRepositories
   (s/optional-key :synced-repo) repo/OrganizedRepositories})

(def GitDomain
  {s/Keyword                 ;represents the user-name
   UserGit})

(def GitDomainResolved (secret/create-resolved-schema GitDomain))

(def InfraResult
     (merge
       {infra/facility infra/GitInfra}
       spec-domain/InfraResult))

(def repo-directory-name repo/repo-directory-name)

(defn-
  configuration
  [user-config]
  (let [{:keys [user-email signing-key diff-tool]} user-config]
    (merge
      {:email user-email}
      (when (contains? user-config :signing-key)
        {:signing-key signing-key})
      (when (contains? user-config :diff-tool)
        {:diff-tool diff-tool}))))

(defn-
  infra-configuration-per-user
  [user
   user-config]
  (let [{:keys [user-email signing-key diff-tool credential
                repo synced-repo]} user-config]
    {:config (configuration user-config)
     :file-fact-keyword spec-domain/fact-id-file
     :trust (repo/trust
              (reduce-kv
                (fn [c k v] (into c v))
                []
                (mu/deep-merge repo synced-repo)))
     :repo (into
             (repo/infra-repos user false credential repo)
             (repo/infra-repos user true credential synced-repo))}))

(defn-
  infra-facts-per-user
  [user
   user-config]
  (let [{:keys [user-email signing-key diff-tool credential
                repo synced-repo]} user-config]
    {:file-fact (merge
                  (repo/infra-facts user repo)
                  (repo/infra-facts user synced-repo))}))

(s/defn ^:always-validate
  infra-configuration
  [domain-config :- GitDomainResolved]
  {infra/facility
    (into {}
      (map
        (fn [[k v]] [k (infra-configuration-per-user k v)])
        domain-config))
   dda.pallet.dda-serverspec-crate.infra/facility
    (apply merge {}
      (map
        (fn [[k v]] (infra-facts-per-user k v))
        domain-config))})
