; Copyright (c) meissa GmbH. All rights reserved.
; You must not remove this notice, or any other, from this software.

(ns dda.pallet.domain.dda-git-crate
  (:require
    [pallet.actions :as actions]
    [pallet.api :as api]
    [schema.core :as s]
    [org.domaindrivenarchitecture.config.commons.map-utils :as map-utils]
    [org.domaindrivenarchitecture.pallet.core.dda-crate :as dda-crate]
    [org.domaindrivenarchitecture.pallet.crate.config :as config-crate]
    [dda.pallet.crate.dda-git-crate :as git-crate]))


(def GitDomainConfig
  {:local-root s/Str
   (s/optional-key :credentials)
   {(s/enum :gitblit :github) {:user s/Str
                               (s/optional-key :password) s/Str}}
   :repo-groups (hash-set (s/enum :dda-pallet))})

(def GitCrateStackConfig
  {:group-specific-config
   {:dda-git-group {:dda-git git-crate/GitConfig}}})

(def
  {:local-dir "/home/ubuntu/code/dda-pallet"
   :credentials {:gitblit {:user "user" :password "pass"}
                 :github {}}
   :repo-urls domain-repos})

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
  {:group-specific-config
   {:dda-git-group
    {:dda-git {:ubuntu {:trust [{:pin-fqdn-or-ip "github.com"}]
                        :repo [{:fqdn "github.com"
                                :orga "DomainDrivenArchitecture"
                                :repo "/dda-config-commons.git"
                                :local-dir "/home/ubuntu/code/dda-pallet/dda-config-commons"
                                :user-credentials {}
                                :server-type :github
                                :transport-type :https-public}]}}}}})

(s/defn ^:always-validate dda-git-group
  [stack-config :- GitCrateStackConfig]
  (let []
    (api/group-spec
      "dda-git-group"
      :extends [(config-crate/with-config stack-config)
                git-crate/with-git])))
