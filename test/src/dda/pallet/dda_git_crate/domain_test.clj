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
   [data-test :refer :all]
   [schema.core :as s]
   [dda.pallet.dda-git-crate.domain :as sut]))


(def invalid
  {:domain-input {:test-user {:invalid 42}}
   :infra {}})

(deftest invalid-test
 (testing
   "test infra-configuration"
    (is (thrown? Exception (sut/infra-configuration
                             (:domain-input invalid))))))

(defdatatest should-generate-infra-with-minimal-input [input expected]
  (is (= expected
         (sut/infra-configuration input))))

(defdatatest should-generate-infra-with-multiuser-input [input expected]
  (is (= expected
         (sut/infra-configuration input))))

(defdatatest should-generate-infra-with-trust-input [input expected]
 (is (= expected
        (get-in
         (sut/infra-configuration input)
         [:dda-git :test-user :trust]))))

(defdatatest should-generate-infra-with-repo-input [input expected]
  (is (= (:infra-repo-expectation expected)
           (get-in (sut/infra-configuration input)
                   [:dda-git :test-user :repo])))
    (is (= (:infra-fact-expectation expected)
           (get-in (sut/infra-configuration input)
                   [:dda-servertest]))))
