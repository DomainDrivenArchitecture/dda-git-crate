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
    [pallet.repl :as pr]
    [clojure.inspector :as inspector]
    [dda.config.commons.map-utils :as mu]
    [org.domaindrivenarchitecture.pallet.commons.session-tools :as session-tools]
    [org.domaindrivenarchitecture.pallet.commons.pallet-schema :as ps]
    [dda.cm.operation :as operation]
    [dda.cm.existing :as existing]
    [dda.pallet.dda-git-crate.app.user-app :as app]))

(def ssh-pub-key
  {:type "ssh-rsa"
   :public-key "AAAAB3NzaC1yc2EAAAADAQABAAABAQCeO+eiYDonq3OfxyaUx259y/1OqbhLciD4UlCkguD5PgOuXw+kCXS1Wbdor9cvU8HnsL2j70sPSwCWkcDrrGQ0kpC0GuNO47pKawAOSv07ELpSIIp/nPK5AX2+qI1H3MADBWBE5N1L7sdgatON2A/cC3u5pzcWDaEH7/IJdOkRm8H+qqG+uva6ceFUoYFiJKDixmsmaUXhhDcfYhfpAPBUCSes+HTeT/hk6pdLTX9xXd4H5wyAc+j1e6kPq9ZcxvzZNr9qEMIFjnNL/S9w1ozxQa3sKJQHj8SyVZDlwjvepGS7fKrdlRps938A7I3Y4BaXGX//M1y2HNbUWbMOllLL"
   :comment "test-users-key"})

(def user-config
  {:user-name {:encrypted-password "xxxx"
               :authorized-keys [ssh-pub-key]}})

(def git-config
  {:os-user :user-name
   :user-email "user-name@some-domain.org"
   :repo-groups #{:dda-pallet}})

(def provisioning-ip
     "192.168.56.104")

(def provisioning-user
 {:login "initial"
  :password "secure1234"})

(def provider
 (existing/provider provisioning-ip "node-id" "dda-git-group"))

(defn provisioning-spec []
  (merge
    (app/git-group-spec (app/app-configuration git-config user-config))
    (existing/node-spec provisioning-user)))

(defn apply-install []
  (pr/session-summary
    (operation/do-apply-install provider (provisioning-spec))))

(defn apply-config []
  (pr/session-summary
    (operation/do-apply-configure provider (provisioning-spec))))

(defn server-test []
  (pr/session-summary
    (operation/do-server-test provider (provisioning-spec))))
