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
(ns dda.pallet.dda-git-crate.domain.repo-test
  (:require
    [clojure.test :refer :all]
    [schema.core :as s]
    [dda.pallet.dda-git-crate.domain.repo :as sut]))

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
            :local-dir "/home/ubuntu/code/dda-pallet/repo1"
            :settings #{:sync}}]
          (sut/collect-repo
            repo-credentials
            true
            "/home/ubuntu/code/"
            {:dda-pallet [gitblit-ssh]})))
  (testing
    "test gitblit-public-https"
      (is (=
           [{:repo "https://fqdn/r/repo2.git",
             :local-dir "/home/ubuntu/code/dda-pallet/repo2"
             :settings #{}}]
           (sut/collect-repo
             repo-credentials
             false
             "/home/ubuntu/code/"
             {:dda-pallet [gitblit-public-https]}))))
  (testing
   "test gitblit-private-https"
     (is (=
          [{:repo "https://user:pass@fqdn/r/repo3.git",
            :local-dir "/home/ubuntu/code/dda-pallet/repo3"
            :settings #{}}]
          (sut/collect-repo
            repo-credentials
            false
            "/home/ubuntu/code/"
            {:dda-pallet [gitblit-private-https]}))))
  (testing
    "test github-ssh"
      (is (=
           [{:repo "ssh://git@github.com:orga/repo4.git",
             :local-dir "/home/ubuntu/code/dda-pallet/repo4"
             :settings #{}}]
           (sut/collect-repo
             repo-credentials
             false
             "/home/ubuntu/code/"
             {:dda-pallet [github-ssh]}))))
  (testing
   "test github-public-https"
     (is (=
          [{:repo "https://github.com/orga/repo5.git",
            :local-dir "/home/ubuntu/code/dda-pallet/repo5"
            :settings #{}}]
          (sut/collect-repo
            repo-credentials
            false
            "/home/ubuntu/code/"
            {:dda-pallet [github-public-https]}))))))

(def config1 {:fqdn "fqdn"
              :ssh-port "29418"
              :repo "repo.git"
              :local-dir "/home/x/code/y"
              :user-credentials {:user "user"}
              :server-type :gitblit
              :transport-type :ssh})

(def config2 {:fqdn "fqdn"
              :ssh-port "29418"
              :repo "repo.git"
              :local-dir "/home/x/code/y"
              :user-credentials {:user "user"}
              :server-type :gitblit
              :transport-type :https-public})

(def config3 {:fqdn "fqdn"
              :ssh-port "29418"
              :repo "repo.git"
              :local-dir "/home/x/code/y"
              :user-credentials {:user "user"
                                 :password "pass"}
              :server-type :gitblit
              :transport-type :https-private})

(def config4 {:fqdn "github.com"
              :orga "orga"
              :repo "repo.git"
              :local-dir "/home/x/code/y"
              :user-credentials {:user "git"}
              :server-type :github
              :transport-type :ssh})

(def config5 {:fqdn "github.com"
              :orga "orga"
              :repo "repo.git"
              :local-dir "/home/x/code/y"
              :user-credentials {}
              :server-type :github
              :transport-type :https-public})

(def bad-config1 {:fqdn "github.com"
                  :repo "repo.git"
                  :local-dir "/home/x/code/y"
                  :user-credentials {:password "orga missing and password not needed"}
                  :server-type :github
                  :transport-type :https-public})

(def bad-config2 {:fqdn "github.com"
                  :orga "orga"
                  :repo "repo.git"
                  :local-dir "/home/x/code/y"
                  :user-credentials {:user "github ssh user is allways git"}
                  :server-type :github
                  :transport-type :ssh})

(def bad-config3 {:fqdn "fqdn"
                  :ssh-port "29418"
                  :repo "repo.git"
                  :local-dir "/home/x/code/y"
                  :user-credentials {:password "user is needed for gitblit ssh"}
                  :server-type :gitblit
                  :transport-type :ssh})

(deftest plan-def
  (testing
    "test plan-def"
      (is (=
            "ssh://user@fqdn:29418/repo.git"
            (sut/git-url config1)))
      (is (=
            "https://fqdn/r/repo.git"
            (sut/git-url config2)))
      (is (=
            "https://user:pass@fqdn/r/repo.git"
            (sut/git-url config3)))
      (is (=
            "ssh://git@github.com:orga/repo.git"
            (sut/git-url config4)))
      (is (=
            "https://github.com/orga/repo.git"
            (sut/git-url config5)))))
