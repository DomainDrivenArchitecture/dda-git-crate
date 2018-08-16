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
         (sut/server-identity-port {:port 42 :access-type :ssh})))
   (is (= 42
         (sut/server-identity-port {:port 42 :access-type :https})))
   (is (= 443
         (sut/server-identity-port {:access-type :https})))
   (is (= 22
         (sut/server-identity-port {:access-type :ssh})))))

(deftest test-reduce-trust-map
 (testing
   (is (= {:github.com_443 {:port 443 :host "github.com"}}
          (sut/reduce-trust-map
            {}
            0
            {:host "github.com"
             :repo-name ""
             :access-type :https
             :server-type :github})))))

(deftest test-trust
 (testing
   (is (= [{:pin-fqdn-or-ip {:port 443 :host "github.com"}}]
          (sut/trust [{:host "github.com"
                       :repo-name ""
                       :access-type :https
                       :server-type :github}])))))