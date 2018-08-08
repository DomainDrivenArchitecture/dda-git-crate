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
(ns dda.pallet.dda-git-crate.infra.server-trust
  (:require
    [clojure.tools.logging :as logging]
    [schema.core :as s]
    [pallet.actions :as actions]
    [dda.config.commons.user-home :as user-home]))

(def PinElement
  {:host s/Str :port s/Num})

(def ServerTrust
  {(s/optional-key :pin-fqdn-or-ip) PinElement
   (s/optional-key :fingerprint) s/Str})

(s/defn
  add-fingerprint-to-known-hosts
  "add a node qualified by ip or fqdn to the users ~/.ssh/known_hosts file."
  [facility :- s/Keyword
   user-name  :- s/Str
   fingerprint]
  (actions/as-action
    (logging/info (str facility "-configure user: add-fingerprint-to-known-hosts")))
  (actions/exec-checked-script
    "add fingerprint to known_hosts"
    ("su" ~user-name "-c" "\"echo " ~fingerprint " >> ~/.ssh/known_hosts\"")))

(s/defn
  add-pinned-fqdn-or-ip
  "add a node qualified by ip or fqdn to the users ~/.ssh/known_hosts file."
  [facility :- s/Keyword
   user-name :- s/Str
   pin-element :- PinElement]
  (let [{:keys [host port]} pin-element]
    (actions/as-action
      (logging/info (str facility "-configure user: add-pinned-fqdn-or-ip")))
    (actions/exec-checked-script
      "add delivered key to known_hosts"
      ("su" ~user-name "-c" "\"ssh-keyscan -p " ~port "-H" ~host ">> ~/.ssh/known_hosts\""))))

(s/defn configure-user
  [facility :- s/Keyword
   user-name :- s/Str
   trusts :- [ServerTrust]]
  (doseq [trust-element trusts]
    (let [{:keys [pin-fqdn-or-ip fingerprint]} trust-element]
      (actions/directory
        (str (user-home/user-home-dir user-name) "/.ssh")
        :owner user-name :group user-name)
      (when (contains? trust-element :pin-fqdn-or-ip)
        (add-pinned-fqdn-or-ip facility user-name pin-fqdn-or-ip))
      (when (contains? trust-element :fingerprint)
        (add-fingerprint-to-known-hosts facility user-name fingerprint)))))
