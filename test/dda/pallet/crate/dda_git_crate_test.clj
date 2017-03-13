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

(deftest plan-def
  (testing 
    "test plan-def" 
      (is (=
            "ssh://jem@repository.domaindrivenarchitecture.org:29418/hewater/meissa-sugar-module.git"
            (sut/git-url :ssh+key
                         {:ssh "ssh://repository.domaindrivenarchitecture.org:29418/hewater/meissa-sugar-module.git"
                          :https "https://repository.domaindrivenarchitecture.org/r/hewater/meissa-sugar-module.git"
                          :user "jem"
                          :password "pass"})))
      (is (=
            "https://jem:pass@repository.domaindrivenarchitecture.org/r/hewater/meissa-sugar-module.git"
            (sut/git-url :https+pass
                         {:ssh "ssh://repository.domaindrivenarchitecture.org:29418/hewater/meissa-sugar-module.git"
                          :https "https://repository.domaindrivenarchitecture.org/r/hewater/meissa-sugar-module.git"
                          :user "jem"
                          :password "pass"})))
      ))
