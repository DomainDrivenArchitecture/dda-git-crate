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
(ns dda.pallet.crate.dda-git-crate-test
  (:require
    [clojure.test :refer :all]
    [schema.core :as s]
    [dda.pallet.crate.dda-git-crate :as sut]))
  
(def config1 {:ssh-url "fqdn:29418/repo.git"
              :https-url "fqdn/r/repo.git"
              :server-type :gitblit
              :ssh {:user "user"}})
  
(def config2 {:ssh-url "fqdn:29418/repo.git"
              :https-url "fqdn/r/repo.git"
              :server-type :gitblit
              :https-public {}})
  
(def config3 {:ssh-url "fqdn:29418/repo.git"
              :https-url "fqdn/r/repo.git"
              :server-type :gitblit
              :https-private {:user "user"
                              :password "pass"}})
   
(def config4 {:ssh-url "github.com:Orga/repo.git"
              :https-url "github.com/Orga/repo.git"
              :server-type :github
              :ssh {}})

(def config5 {:ssh-url ""
              :https-url ""
              :server-type :gitblit
              :ssh {}})
  
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
            "ssh://git@github.com:Orga/repo.git"
            (sut/git-url config4)))
      
      (is (thrown? IllegalArgumentException
                   (sut/git-url config5)))      
      ))
