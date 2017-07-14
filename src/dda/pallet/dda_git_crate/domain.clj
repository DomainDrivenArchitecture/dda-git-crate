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

(ns dda.pallet.dda-git-crate.domain
  (:require
   [pallet.actions :as actions]
   [pallet.api :as api]
   [schema.core :as s]
   [org.domaindrivenarchitecture.config.commons.map-utils :as map-utils]
   [dda.pallet.core.dda-crate :as dda-crate]
   [dda.pallet.crate.config :as config-crate]
   [dda.pallet.dda-git-crate.infra :as git-crate]
   [dda.pallet.dda-git-crate.domain.git-url :as git-url]
   [dda.pallet.dda-git-crate.domain.schema :as domain-schema]
   [dda.pallet.dda-git-crate.domain.repo :as repo]))

(def GitDomainConfig
  domain-schema/GitDomainConfig)

(def GitCrateStackConfig
  {:group-specific-config
   {:dda-git-group {:dda-git git-crate/GitConfig}}})

(def dda-projects
  {:dda-pallet
   ["https://github.com/DomainDrivenArchitecture/dda-pallet-commons.git"
    "https://github.com/DomainDrivenArchitecture/dda-pallet.git"
    "https://github.com/DomainDrivenArchitecture/dda-user-crate.git"
    "https://github.com/DomainDrivenArchitecture/dda-iptables-crate.git"
    "https://github.com/DomainDrivenArchitecture/dda-hardening-crate.git"
    "https://github.com/DomainDrivenArchitecture/dda-provider-crate.git"
    "https://github.com/DomainDrivenArchitecture/dda-init-crate.git"
    "https://github.com/DomainDrivenArchitecture/dda-backup-crate.git"
    "https://github.com/DomainDrivenArchitecture/dda-mysql-crate.git"
    "https://github.com/DomainDrivenArchitecture/httpd-crate.git"
    "https://github.com/DomainDrivenArchitecture/dda-httpd-crate.git"
    "https://github.com/DomainDrivenArchitecture/dda-tomcat-crate.git"
    "https://github.com/DomainDrivenArchitecture/dda-liferay-crate.git"
    "https://github.com/DomainDrivenArchitecture/dda-linkeddata-crate.git"
    "https://github.com/DomainDrivenArchitecture/dda-git-crate.git"
    "https://github.com/DomainDrivenArchitecture/dda-managed-vm.git"
    "https://github.com/DomainDrivenArchitecture/dda-managed-ide.git"
    "https://github.com/DomainDrivenArchitecture/dda-pallet-masterbuild.git"]})

(s/defn ^:always-validate dda-git-crate-stack-configuration :- GitCrateStackConfig
  [convention-config :- GitDomainConfig]
  (let [{:keys [os-user user-email repo-groups credentials]} convention-config
        repos dda-projects]
    {:group-specific-config
      {:dda-git-group
        {:dda-git
          {os-user {
                    :email user-email
                    :trust (repo/collect-trust (first (vals repos)))
                    :repo  (repo/collect-repo
                            credentials
                            (str "/home/" (name os-user) "/code/")
                            repos)}}}}}))

(s/defn ^:always-validate dda-git-group
  [stack-config :- GitCrateStackConfig]
  (let []
    (api/group-spec
      "dda-git-group"
      :extends [(config-crate/with-config stack-config)
                git-crate/with-git])))
