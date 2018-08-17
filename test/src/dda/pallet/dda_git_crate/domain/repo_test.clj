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

(def pwd-authoriezd-https-github
  {:repo-input {:host "github.com" :port 443
                :orga-path "DomainDrivenArchitecture"
                :repo-name "dda-git-crate"
                :protocol :https :server-type :github}
   :credential-input {:github.com_443 {:user-name "test" :password "pwd"}}
   :expected {:repo "https://test:pwd@github.com:443/DomainDrivenArchitecture/dda-git-crate.git"
              :local-dir "/home/test-user/repos/folder1/dda-git-crate"
              :settings #{}}})

(def uname-https-github
  {:repo-input {:host "github.com" :port 443
                :orga-path "DomainDrivenArchitecture"
                :repo-name "dda-git-crate"
                :protocol :https :server-type :github}
   :credential-input {:github.com_443 {:user-name "test"}}
   :expected {:repo "https://test@github.com:443/DomainDrivenArchitecture/dda-git-crate.git"
              :local-dir "/home/test-user/repos/folder1/dda-git-crate"
              :settings #{}}})

(def ssh-github
  {:repo-input {:host "github.com"
                :orga-path "DomainDrivenArchitecture"
                :repo-name "dda-git-crate"
                :protocol :ssh :server-type :github}
   :credential-input {:github.com_22 {:user-name "ssh_test"}}
   :expected {:repo "git@github.com:DomainDrivenArchitecture/dda-git-crate.git"
              :local-dir "/home/test-user/repos/folder1/dda-git-crate"
              :settings #{}}})

(def minimal-https-gitblit
  {:repo-input {:host "repo.meissa-gmbh.de" :repo-name "a-private-repo"
                :orga-path "meissa/group" :protocol :https
                :server-type :gitblit}
   :credential-input nil
   :expected   {:repo "https://repo.meissa-gmbh.de:443/r/meissa/group/a-private-repo.git"
                :local-dir "/home/test-user/repos/folder1/a-private-repo"
                :settings #{:sync}}})

(deftest test-github-repo
 (testing
   (is (= (:expected minimal-https-github)
          (sut/infra-repo
              :test-user
              false
              :folder1
              (:credential-input minimal-https-github)
              (:repo-input minimal-https-github))))
   (is (= (:expected pwd-authoriezd-https-github)
          (sut/infra-repo
              :test-user false :folder1
              (:credential-input pwd-authoriezd-https-github)
              (:repo-input pwd-authoriezd-https-github))))
   (is (= (:expected uname-https-github)
          (sut/infra-repo
              :test-user false :folder1
              (:credential-input uname-https-github)
              (:repo-input uname-https-github))))
   (is (= (:expected ssh-github)
          (sut/infra-repo
              :test-user false :folder1
              (:credential-input ssh-github)
              (:repo-input ssh-github))))))

(def minimal-https-gitblit
  {:repo-input {:host "repo.meissa-gmbh.de" :repo-name "a-private-repo"
                :orga-path "meissa/group" :protocol :https
                :server-type :gitblit}
   :credential-input nil
   :expected   {:repo "https://repo.meissa-gmbh.de:443/r/meissa/group/a-private-repo.git"
                :local-dir "/home/test-user/repos/folder1/a-private-repo"
                :settings #{:sync}}})

(def pwd-authoriezd-https-gitblit
  {:repo-input {:host "repo.meissa-gmbh.de" :repo-name "a-private-repo"
                :orga-path "meissa/group" :protocol :https
                :server-type :gitblit}
   :credential-input {:github.com_443 {:user-name "test" :password "pwd"}
                      :repo.meissa-gmbh.de_443 {:user-name "test2" :password "pwd2"}}
   :expected   {:repo "https://test2:pwd2@repo.meissa-gmbh.de:443/r/meissa/group/a-private-repo.git"
                :local-dir "/home/test-user/repos/folder1/a-private-repo"
                :settings #{:sync}}})

(def ssh-gitblit
  {:repo-input {:host "repo.meissa-gmbh.de" :repo-name "a-private-repo"
                :port 29418 :orga-path "meissa/group" :protocol :ssh
                :server-type :gitblit}
   :credential-input {:github.com_443 {:user-name "test" :password "pwd"}
                      :repo.meissa-gmbh.de_29418 {:user-name "test2" :password "pwd2"}}
   :expected   {:repo "ssh://test2@repo.meissa-gmbh.de:29418/meissa/group/a-private-repo.git"
                :local-dir "/home/test-user/repos/folder1/a-private-repo"
                :settings #{:sync}}})

(deftest test-gitblit-repo
 (testing
   (is (= (:expected minimal-https-gitblit)
          (sut/infra-repo
              :test-user true :folder1
              (:credential-input minimal-https-gitblit)
              (:repo-input minimal-https-gitblit))))
   (is (= (:expected pwd-authoriezd-https-gitblit)
          (sut/infra-repo
              :test-user true :folder1
              (:credential-input pwd-authoriezd-https-gitblit)
              (:repo-input pwd-authoriezd-https-gitblit))))
   (is (= (:expected ssh-gitblit)
          (sut/infra-repo
              :test-user true :folder1
              (:credential-input ssh-gitblit)
              (:repo-input ssh-gitblit))))))

(def minimal-https-gitlab
  {:repo-input {:host "gitlab.com" :repo-name "gitlab-ce"
                :orga-path "gitlab-org" :protocol :https
                :server-type :gitlab}
   :credential-input nil
   :expected   {:repo "https://gitlab.com:443/gitlab-org/gitlab-ce.git"
                :local-dir "/home/test-user/repos/folder1/gitlab-ce"
                :settings #{}}})

(def ssh-gitlab
  {:repo-input {:host "gitlab.meissa-gmbh.de" :repo-name "a-private-repo"
                :orga-path "group" :protocol :ssh
                :server-type :gitlab}
   :credential-input {:github.com_443 {:user-name "test" :password "pwd"}
                      :gitlab.meissa-gmbh.de_22 {:user-name "test2" :password "pwd2"}}
   :expected   {:repo "ssh://git@gitlab.meissa-gmbh.de:group/a-private-repo.git"
                :local-dir "/home/test-user/repos/folder1/a-private-repo"
                :settings #{}}})

(deftest test-gitlab-repo
 (testing
   (is (= (:expected minimal-https-gitlab)
          (sut/infra-repo
              :test-user false :folder1
              (:credential-input minimal-https-gitlab)
              (:repo-input minimal-https-gitlab))))
   (is (= (:expected ssh-gitlab)
          (sut/infra-repo
              :test-user false :folder1
              (:credential-input ssh-gitlab)
              (:repo-input ssh-gitlab))))))
