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
(ns dda.pallet.dda-git-crate.app-test
  (:require
    [clojure.test :refer :all]
    [schema.core :as s]
    [dda.pallet.dda-git-crate.app :as sut]))

(s/set-fn-validation! true)

(deftest test-schema
  (testing
    (is (s/validate
          sut/GitDomain
          {:test-user
            {:user-email "test-user@domain"
             :credential [{:host "github.com"
                           :protocol :https
                           :user-name {:plain "githubtest"}
                           :password {:plain "secure1234"}}]
             :repo {:folder1 [{:host "github.com"
                               :port 443
                               :orga-path "DomainDrivenArchitecture"
                               :repo-name "dda-git-crate"
                               :protocol :https
                               :server-type :github}]}}}))))

(deftest plan-def
  (testing
    "test plan-def"
      (is (map? sut/with-git))))
