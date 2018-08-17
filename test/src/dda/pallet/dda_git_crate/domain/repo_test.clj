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

(deftest test-server-identity-port
 (testing
   (is (= 42
         (sut/server-identity-port {:port 42 :protocol :ssh})))
   (is (= 42
         (sut/server-identity-port {:port 42 :protocol :https})))
   (is (= 443
         (sut/server-identity-port {:protocol :https})))
   (is (= 22
         (sut/server-identity-port {:protocol :ssh})))))

(deftest test-reduce-trust-map
 (testing
   (is (= {:github.com_443 {:port 443 :host "github.com"}}
          (sut/reduce-trust-map
            {}
            0
            {:host "github.com"
             :repo-name ""
             :protocol :https
             :server-type :github})))))

(deftest test-trust
 (testing
   (is (= [{:pin-fqdn-or-ip {:port 443 :host "github.com"}}]
          (sut/trust [{:host "github.com"
                       :repo-name ""
                       :protocol :https
                       :server-type :github}])))))

(def minimal-https-github
  {:repo-input {:host "github.com" :port 443
                :orga-path "DomainDrivenArchitecture"
                :repo-name "dda-git-crate"
                :protocol :https :server-type :github}
   :credential-input nil
   :expected {:repo "https://github.com:443/DomainDrivenArchitecture/dda-git-crate.git"
              :local-dir "/home/test-user/repos/folder1/dda-git-crate"
              :settings #{}}})

(def autoriezd-https-github
  {:repo-input {:host "github.com" :port 443
                :orga-path "DomainDrivenArchitecture"
                :repo-name "dda-git-crate"
                :protocol :https :server-type :github}
   :credential-input {:github.com_443 {:user-name "test" :password "pwd"}}
   :expected {:repo "https://test:pwd@github.com:443/DomainDrivenArchitecture/dda-git-crate.git"
              :local-dir "/home/test-user/repos/folder1/dda-git-crate"
              :settings #{}}})

(def minimal-https-giblit
  {:repo-input {:host "repo.meissa-gmbh.de" :repo-name "a-private-repo"
                :orga-path "meissa/group" :protocol :https
                :server-type :gitblit}
   :credential-input nil
   :expected   {:repo "https://repo.meissa-gmbh.de:443/r/meissa/group/a-private-repo.git"
                :local-dir "/home/test-user/repos/folder1/a-private-repo"
                :settings #{:sync}}})


(deftest test-repo
 (testing
   (is (= (:expected minimal-https-github)
          (sut/infra-repo
              :test-user
              false
              :folder1
              (:credential-input minimal-https-github)
              (:repo-input minimal-https-github))))
   (is (= (:expected autoriezd-https-github)
          (sut/infra-repo
              :test-user false :folder1
              (:credential-input autoriezd-https-github)
              (:repo-input autoriezd-https-github))))
   (is (= (:expected minimal-https-giblit)
          (sut/infra-repo
              :test-user true :folder1
              (:credential-input minimal-https-giblit)
              (:repo-input minimal-https-giblit))))))
