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
(ns dda.pallet.dda-git-crate.app.instantiate-aws
  (:require
    [clojure.inspector :as inspector]
    [schema.core :as s]
    [pallet.api :as api]
    [dda.config.commons.map-utils :as mu]
    [pallet.compute :as compute]
    [org.domaindrivenarchitecture.pallet.commons.encrypted-credentials :as crypto]
    [org.domaindrivenarchitecture.pallet.commons.session-tools :as session-tools]
    [org.domaindrivenarchitecture.pallet.commons.pallet-schema :as ps]
    [dda.cm.operation :as operation]
    [dda.cm.aws :as cloud-target]
    [dda.pallet.crate.config :as config-crate]
    [dda.pallet.domain.dda-user-crate :as user]
    [dda.pallet.dda-git-crate.infra :as git-crate]
    [dda.pallet.domain.dda-servertest-crate :as server-test-domain]
    [dda.pallet.crate.dda-servertest-crate :as server-test-crate]
    [dda.pallet.dda-git-crate.domain :as domain]))

(def jem-key-vm
  {:type "ssh-rsa"
   :public-key "AAAAB3NzaC1yc2EAAAADAQABAAABAQCeO+eiYDonq3OfxyaUx259y/1OqbhLciD4UlCkguD5PgOuXw+kCXS1Wbdor9cvU8HnsL2j70sPSwCWkcDrrGQ0kpC0GuNO47pKawAOSv07ELpSIIp/nPK5AX2+qI1H3MADBWBE5N1L7sdgatON2A/cC3u5pzcWDaEH7/IJdOkRm8H+qqG+uva6ceFUoYFiJKDixmsmaUXhhDcfYhfpAPBUCSes+HTeT/hk6pdLTX9xXd4H5wyAc+j1e6kPq9ZcxvzZNr9qEMIFjnNL/S9w1ozxQa3sKJQHj8SyVZDlwjvepGS7fKrdlRps938A7I3Y4BaXGX//M1y2HNbUWbMOllLL"
   :comment "mje@jergerProject"})

(def user-config
  {:jem {:encrypted-password "xxxx"
         :authorized-keys [jem-key-vm]}})

(def git-config
  {:os-user :jem
   :user-email "jem@domain"
   :repo-groups #{:dda-pallet}})

(def test-config
  {:file {:ubuntu-code {:path "/home/jem/code"
                        :exist? true}}})

(defn group-configuration []
  (mu/deep-merge
    (user/crate-stack-configuration
     user-config :group-key :dda-git-group)
   (domain/dda-git-crate-stack-configuration
    git-config)
   (server-test-domain/crate-stack-configuration
    test-config :group-key :dda-git-group)))

(defn group [stack-config]
 (let []
   (api/group-spec
     "dda-git-group"
     :extends [(config-crate/with-config stack-config)
               server-test-crate/with-servertest
               user/with-user
               git-crate/with-git])))

(defn integrated-group-spec [count]
  (merge
    (group (group-configuration))
    (cloud-target/node-spec "jem")
    {:count count}))

(defn converge-install
  ([count]
   (operation/do-converge-install (cloud-target/provider) (integrated-group-spec count)))
  ([key-id key-passphrase count]
   (operation/do-converge-install (cloud-target/provider key-id key-passphrase) (integrated-group-spec count))))

(defn server-test
  ([count]
   (operation/do-server-test (cloud-target/provider) (integrated-group-spec count)))
  ([key-id key-passphrase count]
   (operation/do-server-test (cloud-target/provider key-id key-passphrase) (integrated-group-spec count))))
