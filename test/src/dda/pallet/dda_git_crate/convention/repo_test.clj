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
(ns dda.pallet.dda-git-crate.convention.repo-test
  (:require
   [clojure.test :refer :all]
   [data-test :refer :all]
   [schema.core :as s]
   [dda.pallet.dda-git-crate.convention.repo :as sut]))

(deftest test-server-identity-port
  (is (= 42
         (sut/server-identity-port {:port 42 :protocol :ssh})))
  (is (= 42
         (sut/server-identity-port {:port 42 :protocol :https})))
  (is (= 443
         (sut/server-identity-port {:protocol :https})))
  (is (= 22
         (sut/server-identity-port {:protocol :ssh}))))

(deftest test-reduce-trust-map
  (is (= {:github.com_443 {:port 443 :host "github.com"}}
         (sut/reduce-trust-map
          {}
          0
          {:host "github.com"
           :repo-name ""
           :protocol :https
           :server-type :github}))))

(deftest test-trust
  (is (= [{:pin-fqdn-or-ip {:port 443 :host "github.com"}}]
         (sut/trust [{:host "github.com"
                      :repo-name ""
                      :protocol :https
                      :server-type :github}]))))

(defdatatest should-create-repo-infra-for-github [input expected]
 (is (= expected
        (sut/infra-repo
         :test-user false :folder1
         (:credential-input input)
         (:repo-input input)))))
             
(defdatatest should-create-repo-infra-for-gitblit [input expected]
 (is (= expected
        (sut/infra-repo
         :test-user false :folder1
         (:credential-input input)
         (:repo-input input)))))
             
(defdatatest should-create-repo-infra-for-gitlab [input expected]
 (is (= expected
        (sut/infra-repo
         :test-user false :folder1
         (:credential-input input)
         (:repo-input input)))))

(deftest test-infra-fact
 (is (= {:_home_test-user_repo_folder1_gitlab-ce
         {:path "/home/test-user/repo/folder1/gitlab-ce"}}
        (sut/infra-fact
         :test-user
         :folder1
         {:host "gitlab.com" :repo-name "gitlab-ce"
          :orga-path "gitlab-org" :protocol :https
          :server-type :gitlab}))))

(deftest test-infra-facts
  (is (= {:_home_test-user_repo_folder1_gitlab-ce
          {:path "/home/test-user/repo/folder1/gitlab-ce"}
          :_home_test-user_repo_folder1_a-private-repo
          {:path "/home/test-user/repo/folder1/a-private-repo"}}
         (sut/infra-facts
          :test-user
          {:folder1 [{:host "gitlab.com" :repo-name "gitlab-ce"
                      :orga-path "gitlab-org" :protocol :https
                      :server-type :gitlab}
                     {:host "repositories.website.com" :repo-name "a-private-repo"
                      :orga-path "meissa/group" :protocol :https
                      :server-type :gitblit}]}))))
