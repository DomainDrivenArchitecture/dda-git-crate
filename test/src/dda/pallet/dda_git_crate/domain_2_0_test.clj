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
(ns dda.pallet.dda-git-crate.domain-2-0-test
  (:require
    [clojure.test :refer :all]
    [schema.core :as s]
    [dda.pallet.dda-git-crate.domain-2-0 :as sut]))

(def git-config-1
  {:os-user :jem
   :user-email "jem@domain"
   :repo-groups #{:dda-pallet}})

(def git-config-2
 {:os-user :jem
  :user-email "jem@domain"
  :repos {:dda-pallet
          ["https://github.com/DomainDrivenArchitecture/dda-config-commons.git"
           "https://github.com/DomainDrivenArchitecture/dda-pallet-commons.git"]
          :else
          ["ssh://user@fqdn:29418/repo1.git"]}})

(def git-config-3
  {:os-user :jem
   :user-email "jem@domain"})

(deftest infra-configuration-test
 (testing
   "test infra-configuration"
     (is (sut/infra-configuration
            git-config-1))
     (is (sut/infra-configuration
            git-config-2))
     (is (thrown? Exception (sut/infra-configuration
                              git-config-3)))))
