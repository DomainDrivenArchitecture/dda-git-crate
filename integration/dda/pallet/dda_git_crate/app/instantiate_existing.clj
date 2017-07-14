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
(ns dda.pallet.dda-git-crate.app.instantiate-existing
  (:require
    [clojure.inspector :as inspector]
    [schema.core :as s]
    [pallet.api :as api]
    [dda.config.commons.map-utils :as mu]
    [dda.cm.operation :as operation]
    [dda.cm.existing :as existing]
    [dda.pallet.dda-git-crate.app.test-app :as app]))

(def git-config
  {:os-user :ubuntu
   :user-email "ubuntu@domain"
   :repo-groups #{:dda-pallet}})

(def test-config
  {:file {:ubuntu-code {:path "/home/ubuntu/code"
                        :exist? true}}})

(def provisioning-ip
     "52.28.86.52")

(def provisioning-user
 {:login "ubuntu"})
;:login "shantanu"})

(def provider
 (existing/provider provisioning-ip "node-id" "dda-git-group"))

(defn provisioning-spec []
  (merge
    (app/group-spec (app/app-configuration git-config test-config))
    (existing/node-spec provisioning-user)))

(defn apply-install []
  (operation/do-apply-install provider (provisioning-spec)))

(defn apply-config []
  (operation/do-apply-configure provider (provisioning-spec)))

(defn server-test []
  (operation/do-server-test provider (provisioning-spec)))
