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
(ns dda.pallet.dda-git-crate.domain.git-url-test
  (:require
    [clojure.test :refer :all]
    [schema.core :as s]
    [dda.pallet.dda-git-crate.domain.git-url :as sut]))

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
