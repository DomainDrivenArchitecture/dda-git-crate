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
(ns dda.pallet.crate.dda-git-crate.instantiate-existing
  (:require
    [clojure.inspector :as inspector]
    [schema.core :as s]
    [dda.cm.operation :as operation]
    [dda.cm.existing :as existing]
    [dda.pallet.domain.dda-git-crate :as domain]))

(def domain-config
  {:os-user :ubuntu
   :user-email "ubuntu@domain"
   :repo-groups #{:dda-pallet}})

(def provisioning-ip
     "52.28.86.52")

(def provisioning-user
 {:login "ubuntu"})
;:login "shantanu"})

(def provider
 (existing/provider provisioning-ip "node-id" "dda-git-group"))

(defn integrated-group-spec []
  (merge
    (domain/dda-git-group (domain/dda-git-crate-stack-configuration domain-config))
    (existing/node-spec  provisioning-user)))

(defn apply-install []
  (operation/do-apply-install provider (integrated-group-spec)))

(defn apply-config []
  (operation/do-apply-configure provider (integrated-group-spec)))

(defn server-test []
  (operation/do-server-test provider (integrated-group-spec)))
