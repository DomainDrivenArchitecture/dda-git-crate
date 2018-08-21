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

(deftest invalid-test
 (testing
   "test infra-configuration"
    (is (thrown? Exception (sut/infra-configuration
                             (:domain-input invalid))))))

(def git-minimal
  {:domain-input {:test-user {:user-email "test-user@domain"}}
   :infra {:dda-git
           {:test-user
            {:config {:email "test-user@domain"}, :trust [], :repo []}}}})

(deftest minimal-test
 (testing
   (is (= (:infra git-minimal)
          (sut/infra-configuration
            (:domain-input git-minimal))))))

(def git-multiuser
  {:domain-input {:test-user1 {:user-email "test-user1@domain"}
                  :test-user2 {:user-email "test-user2@domain"}}
   :infra {:dda-git
           {:test-user1
            {:config {:email "test-user1@domain"}, :trust [], :repo []}
            :test-user2
            {:config {:email "test-user2@domain"}, :trust [], :repo []}}}})

(deftest multiuser-test
 (testing
   (is (= (:infra git-multiuser)
          (sut/infra-configuration
            (:domain-input git-multiuser))))))

(def trust
  {:domain-input
   {:test-user
     {:user-email "test-user@domain"
      :repo {:folder1 [{:host "github.com"
                        :port 443
                        :orga-path "DomainDrivenArchitecture"
                        :repo-name "dda-git-crate"
                        :protocol :https
                        :server-type :github}
                       {:host "github.com"
                        :orga-path "DomainDrivenArchitecture"
                        :repo-name "dda-serverspec-crate"
                        :protocol :https
                        :server-type :github}]
              :folder2 [{:host "github.com"
                         :orga-path "DomainDrivenArchitecture"
                         :repo-name "dda-managed-ide"
                         :protocol :ssh
                         :server-type :github}]}
      :synced-repo {:folder1 [{:host "repo.meissa-gmbh.de"
                               :repo-name "a-private-repo"
                               :protocol :ssh
                               :server-type :gitblit}]}}}
   :infra-trust [{:pin-fqdn-or-ip {:port 443 :host "github.com"}}
                 {:pin-fqdn-or-ip {:port 22 :host "repo.meissa-gmbh.de"}}
                 {:pin-fqdn-or-ip {:port 22 :host "github.com"}}]})

(deftest trust-test
 (testing
   (is (= (:infra-trust trust)
          (get-in
            (sut/infra-configuration
              (:domain-input trust))
            [:dda-git :test-user :trust])))))

(def repos
  {:domain-input
   {:test-user
     {:user-email "test-user@domain"
      :credential [{:host "github.com"
                    :protocol :https
                    :user-name "githubtest"
                    :password "secure1234"}]
      :repo {:folder1 [{:host "github.com"
                        :port 443
                        :orga-path "DomainDrivenArchitecture"
                        :repo-name "dda-git-crate"
                        :protocol :https
                        :server-type :github}
                       {:host "github.com"
                        :orga-path "DomainDrivenArchitecture"
                        :repo-name "dda-serverspec-crate"
                        :protocol :https
                        :server-type :github}]
              :folder2 [{:host "github.com"
                         :orga-path "DomainDrivenArchitecture"
                         :repo-name "dda-managed-ide"
                         :protocol :ssh
                         :server-type :github}]}
      :synced-repo {:folder1 [{:host "repo.meissa-gmbh.de"
                               :repo-name "a-private-repo"
                               :orga-path "meissa/group"
                               :protocol :https
                               :server-type :gitblit}]}}}
   :infra-repo-expectation
   [{:repo "https://githubtest:secure1234@github.com:443/DomainDrivenArchitecture/dda-git-crate.git"
     :local-dir "/home/test-user/repo/folder1/dda-git-crate"
     :settings #{}}
    {:repo "https://githubtest:secure1234@github.com:443/DomainDrivenArchitecture/dda-serverspec-crate.git"
     :local-dir "/home/test-user/repo/folder1/dda-serverspec-crate"
     :settings #{}}
    {:repo "git@github.com:DomainDrivenArchitecture/dda-managed-ide.git"
     :local-dir "/home/test-user/repo/folder2/dda-managed-ide"
     :settings #{}}
    {:repo "https://repo.meissa-gmbh.de:443/r/meissa/group/a-private-repo.git"
     :local-dir "/home/test-user/repo/folder1/a-private-repo"
     :settings #{:sync}}]})

(deftest repo-test
  (testing
    (is (= (:infra-repo-expectation repos)
           (get-in (sut/infra-configuration
                     (:domain-input repos))
                   [:dda-git :test-user :repo])))))
