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
(ns dda.pallet.crate.dda-git-crate.instantiate-aws
  (:require
    [clojure.inspector :as inspector]
    [schema.core :as s]
    [pallet.api :as api]
    [pallet.compute :as compute]
    [org.domaindrivenarchitecture.pallet.commons.encrypted-credentials :as crypto]
    [org.domaindrivenarchitecture.pallet.commons.session-tools :as session-tools]
    [org.domaindrivenarchitecture.pallet.commons.pallet-schema :as ps]
    [org.domaindrivenarchitecture.cm.operation :as operation]
    [dda.pallet.crate.dda-git-crate.aws :as cloud-target]
    [dda.pallet.domain.dda-git-crate :as domain]))


(def domain-config
  {:repo-groups #{:dda-pallet}})

(defn integrated-group-spec [count]
  (merge
    (domain/dda-git-group (domain/dda-git-crate-stack-configuration domain-config))
    (cloud-target/node-spec)
    {:count count}))

(defn converge-install
  ([count]
   (operation/do-converge-install (cloud-target/provider) (integrated-group-spec count)))
  ([key-id key-passphrase count]
   (operation/do-converge-install (cloud-target/provider key-id key-passphrase) (integrated-group-spec count))))


(defn server-test
  ([]
   (operation/do-server-test (cloud-target/provider) (integrated-group-spec count)))
  ([key-id key-passphrase]
   (operation/do-server-test (cloud-target/provider) (integrated-group-spec count))))
