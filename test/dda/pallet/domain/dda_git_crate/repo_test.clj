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

(def gitblit-ssh "ssh://user@fqdn:29418/repo1.git")
(def gitblit-public-https "https://fqdn/r/repo2.git")
(def gitblit-private-https "https://user:pass@fqdn/r/repo3.git")
(def github-ssh "ssh://git@github.com:orga/repo4.git")
(def github-public-https "https://github.com/orga/repo5.git")

(def repo-credentials {:gitblit {:user "user" :password "pass"}})

(def domain-repos [gitblit-ssh gitblit-private-https github-public-https github-ssh gitblit-public-https])

(deftest collect-trust-test
  (testing
    "test plan-def"
      (is (=
           [{:pin-fqdn-or-ip "fqdn"}
            {:pin-fqdn-or-ip "github.com"}]
           (sut/collect-trust domain-repos)))))

(deftest collect-repo-test
 (testing
   "test gitblit-ssh"
     (is (=
          [{:repo "ssh://user@fqdn:29418/repo1.git",
            :local-dir "/home/ubuntu/code/dda-pallet/repo1"}]
          (sut/collect-repo
            repo-credentials
            "/home/ubuntu/code/"
            {:dda-pallet [gitblit-ssh]})))
  (testing
    "test gitblit-public-https"
      (is (=
           [{:repo "https://fqdn/r/repo2.git",
             :local-dir "/home/ubuntu/code/dda-pallet/repo2"}]
           (sut/collect-repo
             repo-credentials
             "/home/ubuntu/code/"
             {:dda-pallet [gitblit-public-https]}))))
  (testing
   "test gitblit-private-https"
     (is (=
          [{:repo "https://user:pass@fqdn/r/repo3.git",
            :local-dir "/home/ubuntu/code/dda-pallet/repo3"}]
          (sut/collect-repo
            repo-credentials
            "/home/ubuntu/code/"
            {:dda-pallet [gitblit-private-https]}))))
  (testing
    "test github-ssh"
      (is (=
           [{:repo "ssh://git@github.com:orga/repo4.git",
             :local-dir "/home/ubuntu/code/dda-pallet/repo4"}]
           (sut/collect-repo
             repo-credentials
             "/home/ubuntu/code/"
             {:dda-pallet [github-ssh]}))))
  (testing
   "test github-public-https"
     (is (=
          [{:repo "https://github.com/orga/repo5.git",
            :local-dir "/home/ubuntu/code/dda-pallet/repo5"}]
          (sut/collect-repo
            repo-credentials
            "/home/ubuntu/code/"
            {:dda-pallet [github-public-https]}))))))
