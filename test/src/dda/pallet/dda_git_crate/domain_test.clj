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
(ns dda.pallet.dda-git-crate.domain-test
  (:require
    [clojure.test :refer :all]
    [schema.core :as s]
    [dda.pallet.dda-git-crate.domain :as sut]))

(def invalid
  {:domain-input {:test-user {:invalid 42}}
   :infra {}})

(def git-minimal
  {:domain-input {:test-user {:user-email "test-user@domain"}}
   :infra {:dda-git
           {:test-user
            {:config {:email "test-user@domain"}, :trust [], :repo []}}}})

(def git-multiuser
  {:domain-input {:test-user1 {:user-email "test-user1@domain"}
                  :test-user2 {:user-email "test-user2@domain"}}
   :infra {:dda-git
           {:test-user1
            {:config {:email "test-user1@domain"}, :trust [], :repo []}
            :test-user2
            {:config {:email "test-user2@domain"}, :trust [], :repo []}}}})

(def trust
  {:domain-input
   {:test-user
     {:user-email "test-user@domain"
      :repo {:folder [{:host "github.com"
                       :port 443
                       :orga-path "DomainDrivenArchitecture"
                       :repo-name "dda-git-crate"
                       :access-type :https
                       :server-type :github}]}}}
   :infra {:dda-git
           {:test-user
            {:config {:email "test-user@domain"},
             :trust [{:pin-fqdn-or-ip {:port 443 :host "github.com"}}]
             :repo []}}}})


(deftest invalid-test
 (testing
   "test infra-configuration"
    (is (thrown? Exception (sut/infra-configuration
                             (:domain-input invalid))))))

(deftest minimal-test
 (testing
   (is (= (:infra git-minimal)
          (sut/infra-configuration
            (:domain-input git-minimal))))))

(deftest multiuser-test
 (testing
   (is (= (:infra git-multiuser)
          (sut/infra-configuration
            (:domain-input git-multiuser))))))

(deftest trust-test
 (testing
   (is (= (:infra trust)
          (sut/infra-configuration
            (:domain-input trust))))))
