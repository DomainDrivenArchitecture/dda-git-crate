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
    [schema.core :as s]
    [pallet.actions :as actions]))

(def PinElement
  {:host s/Str :port s/Num})

(defn add-fingerprint-to-known-hosts
  "add a node qualified by ip or fqdn to the users ~/.ssh/known_hosts file."
  [user-name fingerprint]
  (actions/exec-checked-script
    "add fingerprint to known_hosts"
    ("su" ~user-name "-c" "\"echo " ~fingerprint " >> ~/.ssh/known_hosts\"")))

(s/defn
  pin-fqdn-or-ip
  "add a node qualified by ip or fqdn to the users ~/.ssh/known_hosts file."
  [user-name
   pin-element :- PinElement]
  (let [{:keys [host port]} pin-element]
    (actions/exec-checked-script
      "add delivered key to known_hosts"
      ("su" ~user-name "-c" "\"ssh-keyscan -p " ~port "-H" ~host ">> ~/.ssh/known_hosts\""))))
