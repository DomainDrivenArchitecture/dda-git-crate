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

(ns dda.pallet.dda-git-crate.infra.git-config
  (:require
   [schema.core :as s]
   [pallet.actions :as actions]))

(def UserGlobalConfig {:email s/Str
                       (s/optional-key :signing-key) s/Str
                       (s/optional-key :diff-tool) s/Str})

(s/defn
  configure-user
  [user-name :- s/Str
   git-config :- UserGlobalConfig]
  (let [{:keys [email signing-key diff-tool]} git-config]
    (actions/exec-checked-script
      (str "configures git globally for user:" user-name " & " email)
      ("su" ~user-name "-c" "\"git config --global push.default simple\"")
      ("su" ~user-name "-c" "\"git config --global user.name" ~user-name "\"")
      ("su" ~user-name "-c" "\"git config --global user.email" ~email "\""))
    (when (contains? git-config :signing-key)
      (actions/exec-checked-script
        (str "configures git globally for user: " signing-key)
        ("su" ~user-name "-c" "\"git config --global user.signingkey" ~signing-key "\"")))
    (when (contains? git-config :diff-tool)
      (actions/exec-checked-script
        (str "configures git globally for user: "diff-tool)
        ("su" ~user-name "-c" "\"git config --global --add diff.tool" ~diff-tool "\"")))))
