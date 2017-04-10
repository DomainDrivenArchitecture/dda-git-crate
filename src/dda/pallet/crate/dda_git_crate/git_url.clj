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
(ns dda.pallet.crate.dda-git-crate.git-url
  (:require
    [clojure.tools.logging :as logging]
    [schema.core :as s]
    [dda.pallet.crate.dda-git-crate.schema :as git-schema]))

(s/defn git-url :- s/Str
  [repository :- git-schema/GitRepository]
  (let [{:keys [user-credentials fqdn ssh-port repo local-dir
                transport-type server-type orga]} repository
        cred (cond
               (= :https-public transport-type) ""
               (= :https-private transport-type) (str
                                                   (:user user-credentials) ":"
                                                   (:password user-credentials) "@")
               (= :ssh transport-type) (str (:user user-credentials) "@"))
        protocol (if (= :ssh transport-type)
                   "ssh://" "https://")
        base-path (cond
                    (and
                      (= :gitblit server-type)
                      (not (= :ssh transport-type))) "/r"
                    (and
                      (= :github server-type)
                      (= :ssh transport-type)) (str ":" orga)
                    (and
                      (= :github server-type)
                      (not (= :ssh transport-type))) (str "/" orga)
                    :default "")
        server (if (and
                     (= :ssh transport-type)
                     (contains? repository :ssh-port))
                 (str fqdn ":" ssh-port) fqdn)]
    (str protocol cred server base-path "/" repo)))
