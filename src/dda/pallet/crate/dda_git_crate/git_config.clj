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

(ns dda.pallet.crate.dda-git-crate.git-config
  (:require
   [pallet.actions :as actions]))

;todo: git config --global --add diff.tool meld

(defn configure-user [user-name email]
  (actions/exec-checked-script
    "configures git globally for user"
    ("git" "config" "--global" "push.default" "simple")
    ("git" "config" "--global" "user.name" ~user-name)
    ("git" "config" "--global" "user.email" ~email)))
