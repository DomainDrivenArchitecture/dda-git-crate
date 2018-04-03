# dda-git-crate

[![Clojars Project](https://img.shields.io/clojars/v/dda/dda-git-crate.svg)](https://clojars.org/dda/dda-git-crate)
[![Build Status](https://travis-ci.org/DomainDrivenArchitecture/dda-git-crate.svg?branch=master)](https://travis-ci.org/DomainDrivenArchitecture/dda-git-crate)

[![Slack](https://img.shields.io/badge/chat-clojurians-green.svg?style=flat)](https://clojurians.slack.com/messages/#dda-pallet/) | [<img src="https://domaindrivenarchitecture.org/img/meetup.svg" width=50 alt="DevOps Hacking with Clojure Meetup"> DevOps Hacking with Clojure](https://www.meetup.com/de-DE/preview/dda-pallet-DevOps-Hacking-with-Clojure) | [Website & Blog](https://domaindrivenarchitecture.org)

## compatability
dda-pallet is compatible to the following versions
 * pallet 0.8.x
 * clojure 1.7
 * (x)ubuntu 16.04

## Features
 This crate can clone & manage git repositories in name of defined users on target systems. Features are:
 * clone repositories from various git providers
 * configure users global git settings like signing key, name or email
 * establish trust to repository servers
 * auto-sync (pull & push) repositories using cron
 * support ssh, https public, https user/password transport

## Usage
1. Download the jar-file from the releases page of this repository (e.g. dda-git-crate-x.x.x-standalone.jar).
2. Deploy the jar-file on the source machine
3. Create the files `git.edn` (Domain-Schema for your desktop) and `target.edn` (Schema for Targets to be provisioned) according to the reference and our example configurations. Please create them in the same folder where you've saved the jar-file. For more information about these files refer to the corresponding information below.
4. Start the installation:
```bash
java -jar dda-git-crate-standalone.jar --targets targets.edn git.edn
```

### Detailed Howto
You can find a more detailed howto here: https://domaindrivenarchitecture.org/posts/2017-07-28-compose-crates/

### Configuration
The configuration consists of two files defining both WHERE to install the software and WHAT to install.
* `targets.edn`: describes on which target system(s) the software will be installed
* `git.edn`: describes which repositories will be installed

#### Targets config example
Example content of file `targets.edn`:
```clojure
{:existing [{:node-name "test-vm1"            ; semantic name
             :node-ip "35.157.19.218"}]       ; the ip4 address of the machine to be provisioned
 :provisioning-user {:login "initial"         ; account used to provision
                     :password "secure1234"}} ; optional password, if no ssh key is authorized
```

#### VM config example
Example content of file `vm.edn`:
```clojure
{:os-user :ubuntu
 :user-email "ubuntu@some-domain.org"
 :repos {:books
         ["https://github.com/DomainDrivenArchitecture/ddaArchitecture.git"]
         :dda-pallet
         ["https://github.com/DomainDrivenArchitecture/dda-config-commons.git"
          "https://github.com/DomainDrivenArchitecture/dda-pallet-commons.git"]}
  :synced-repos {
          :password-store
          ["https://github.com/DomainDrivenArchitecture/password-store-for-teams.git"]}}
```

## Reference
Some details about the architecture: We provide two levels of API. **Domain** is a high-level API with many build in conventions. If this conventions don't fit your needs, you can use our low-level **infra** API and realize your own conventions.

### Targets
The schema for the targets config is:
```clojure
(def ExistingNode
  "Represents a target node with ip and its name."
  {:node-name s/Str   ; semantic name (keep the default or use a name that suits you)
   :node-ip s/Str})   ; the ip4 address of the machine to be provisioned

(def ExistingNodes
  "A sequence of ExistingNodes."
  {s/Keyword [ExistingNode]})

(def ProvisioningUser
  "User used for provisioning."
  {:login s/Str                                ; user on the target machine, must have sudo rights
   (s/optional-key :password) secret/Secret})  ; password can be ommited, if a ssh key is authorized

(def Targets
  "Targets to be used during provisioning."
  {:existing [ExistingNode]                                ; one ore more target nodes.
   (s/optional-key :provisioning-user) ProvisioningUser})  ; user can be ommited to execute on localhost with current user.
```
The "targets.edn" uses this schema.

### Domain API
```clojure
(def GitRepository
  {:user-credentials {(s/optional-key :user) s/Str
                      (s/optional-key :password) s/Str}
   :fqdn s/Str
   (s/optional-key :ssh-port) s/Str
   (s/optional-key :orga) s/Str
   :repo s/Str
   :local-dir s/Str
   :transport-type (s/enum :ssh :https-public :https-private)
   :server-type (s/enum :gitblit :github)})

(def GitCredentials
  {(s/enum :gitblit :github) {:user s/Str
                              (s/optional-key :password) s/Str}})
(def GitDomainConfig
  (s/both
     (s/pred #(or
               (contains? %1 :repo-groups)
               (contains? %1 :repos))
              "Either :repo-groups or :repos has to be present.")
     {:os-user s/Keyword
      :user-email s/Str
      (s/optional-key :signing-key) s/Str
      (s/optional-key :diff-tool) s/Str
      (s/optional-key :credentials) GitCredentials
      (s/optional-key :repo-groups) (hash-set (s/enum :dda-pallet))
      (s/optional-key :repos) {s/Keyword [s/Str]}
      (s/optional-key :synced-repos) {s/Keyword [s/Str]}}))
```

### Infra API
```clojure
(def ServerTrust
  {(s/optional-key :pin-fqdn-or-ip) s/Str
   (s/optional-key :fingerprint) s/Str})

(def GitRepository
 {:repo s/Str
  :local-dir s/Str
  :settings (hash-set (s/enum :sync))})

(def UserGlobalConfig {:email s/Str
                       (s/optional-key :signing-key) s/Str
                       (s/optional-key :diff-tool) s/Str})

(def UserGitConfig
  {:config UserGlobalConfig
   :trust [ServerTrust]
   :repo [GitRepository]})

(def GitConfig
  {s/Keyword      ; Keyword is user-name
   UserGitConfig})
```

## License
Copyright © 2015 meissa GmbH
Licensed under the Apache License, Version 2.0 (the "License");
