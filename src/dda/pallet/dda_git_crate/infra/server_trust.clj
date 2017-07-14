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
    [pallet.actions :as actions]))

(defn add-fingerprint-to-known-hosts
  "add a node qualified by ip or fqdn to the users ~/.ssh/known_hosts file."
  [fingerprint]
  (actions/exec-checked-script
    "add fingerprint to known_hosts"
    ("echo" ~fingerprint ">>" "~/.ssh/known_hosts")))

(defn add-node-to-known-hosts
  "add a node qualified by ip or fqdn to the users ~/.ssh/known_hosts file."
  [fqdn-or-ip & {:keys [port] :or {port 22}}]
  (actions/exec-checked-script
    "add delivered key to known_hosts"
    ("ssh-keyscan" "-p" ~port "-H" ~fqdn-or-ip ">>" "~/.ssh/known_hosts")))
