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
(ns dda.pallet.crate.dda-git-crate.git-repo
  (:require
    [clojure.tools.logging :as logging]
    [clojure.string :as st]
    [schema.core :as s]
    [pallet.actions :as actions]
    [pallet.crate.git :as git]
    [dda.pallet.crate.dda-git-crate.schema :as git-schema]))

(s/defn project-parent-path
  [repo :- git-schema/GitRepository]
  (let [{:keys [local-dir]} repo]
    (st/join "/" (drop-last (st/split local-dir #"/")))))

(s/defn create-project-parent
  [path :- s/Str]
  (actions/directory path))

(s/defn clone
  [crate-repo :- git-schema/GitRepository]
  (let [{:keys [local-dir repo]} crate-repo]
    (git/clone
      repo
      :checkout-dir local-dir)))
