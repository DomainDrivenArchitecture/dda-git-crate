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
(ns dda.pallet.domain.dda-git-crate.repo-test
  (:require
    [clojure.test :refer :all]
    [schema.core :as s]
    [dda.pallet.domain.dda-git-crate.repo :as sut]))

(def gitblit-ssh "ssh://user@fqdn:29418/repo.git")
(def gitblit-public-https "https://fqdn/r/repo.git")
(def gitblit-private-https "https://user:pass@fqdn/r/repo.git")
(def github-ssh "ssh://git@github.com:orga/repo.git")
(def github-public-https "https://github.com/orga/repo.git")

(def domain-repos [gitblit-ssh gitblit-private-https github-public-https github-ssh gitblit-public-https])

(deftest collect-trust-test
  (testing
    "test plan-def"
      (is (=
           [{:pin-fqdn-or-ip "fqdn"}
            {:pin-fqdn-or-ip "github.com"}]
           (sut/collect-trust domain-repos)))))
