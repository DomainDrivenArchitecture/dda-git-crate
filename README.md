# dda-git-crate
[![Clojars Project](https://img.shields.io/clojars/v/dda/dda-git-crate.svg)](https://clojars.org/dda/dda-git-crate)
[![Build Status](https://travis-ci.org/DomainDrivenArchitecture/dda-git-crate.svg?branch=master)](https://travis-ci.org/DomainDrivenArchitecture/dda-git-crate)

[![Slack](https://img.shields.io/badge/chat-clojurians-green.svg?style=flat)](https://clojurians.slack.com/messages/#dda-pallet/) | [<img src="https://meissa-gmbh.de/img/community/Mastodon_Logotype.svg" width=20 alt="team@social.meissa-gmbh.de"> team@social.meissa-gmbh.de](https://social.meissa-gmbh.de/@team) | [Website & Blog](https://domaindrivenarchitecture.org)

## Jump to
[Compatibility](#compatibility)  
[Features](#features)  
[Usage Summary](#usage-summary)  
[Detailed How-to](#detailed-how-to)  
[Configuration](#configuration)  
[Targets config example](#targets-config-example)  
[Git config example](#git-config-example)  
[Reference-Targets](#targets)  
[Reference-Domain-API](#domain-api)  
[Reference-Infra-API](#infra-api)  
[License](#license)  

## Compatability

dda-pallet is compatible to the following versions
 * pallet 0.9
 * clojure 1.10
 * (x)ubuntu 20.04

## Features

 This crate can clone & manage git repositories in name of defined users on target systems. Features are:
 * clone repositories from various git providers
 * configure users global git settings like signing key, name or email
 * establish trust to repository servers
 * auto-sync (pull & push) repositories using cron
 * support ssh, https public, https user/password transport
 * reconfigure origin remote url of existing repositories

## Usage Summary

1. **Download the jar-file** from the releases page of this repository (e.g. `curl -L -o dda-git-crate.jar https://github.com/DomainDrivenArchitecture/dda-git-crate/releases/download/2.1.0/dda-git-crate-2.1.0-standalone.jar`).
2. Create the files `git.edn` (Domain-Schema for your desktop) and `target.edn` (Schema for Targets to be provisioned) according to the reference and our example configurations. Please create them in the same folder where you've saved the jar-file. For more information about these files refer to the corresponding information below.
3. Start the installation:
```bash
java -jar dda-git-crate.jar --targets targets.edn git.edn
```

### Detailed How-to

You can find a more detailed howto here: https://domaindrivenarchitecture.org/posts/2017-07-28-compose-crates/

## Configuration

The configuration consists of two files defining both WHERE to install the software and WHAT to install.
* `targets.edn`: describes on which target system(s) the software will be installed
* `git.edn`: describes which repositories will be installed

You can download examples of these configuration files from  
[example-targets.edn](example-targets.edn) and   
[example-serverspec.edn](example-serverspec.edn) respectively.

#### Targets config example
Example content of file `targets.edn`:
```clojure
{:existing [{:node-name "test-vm1"            ; semantic name
             :node-ip "35.157.19.218"}]       ; the ip4 address of the machine to be provisioned
 :provisioning-user {:login "initial"         ; account used to provision
                     :password
                     {:plain "secure1234"}}} ; optional password, if no ssh key is authorized
```

#### Git config example

Example content of file `git.edn`:
```clojure
{:initial
 {:user-email "initial@some-domain.org"
  :repo  {:books
          [{:host "github.com"
            :orga-path "DomainDrivenArchitecture"
            :repo-name "ddaArchitecture"
            :protocol :https
            :server-type :github}]
          :dda-pallet
          [{:host "github.com"
            :orga-path "DomainDrivenArchitecture"
            :repo-name "dda-serverspec-crate"
            :protocol :https
            :server-type :github}
  :synced-repo  {:password-store
                 [{:host "github.com"
                   :orga-path "DomainDrivenArchitecture"
                   :repo-name "password-store-for-teams"
                   :protocol :https
                   :server-type :github}]}}}
```

If you want to clone your repositories with **https** from a private git server, you need to add your credentials to your `git.edn` file. You also need to add a **:port**, if it differs from **22** or **443**. For example:
```clojure
{:initial
 {:user-email "initial@some-domain.org"
  :credential [{:user-name {:plain "initial"}
                :password {:plain "githubpassword"}
                :host "gitserver.de"
                :port 123
                :protocol :https}]}}
```

### Watch log for debug reasons

In case any problems occur, you may want to have a look at the log-file:
`less logs/pallet.log`

## Reference

Some details about the architecture: We provide two levels of API.  
* **Domain-Level-API**: Domain is a high-level API with many build in conventions.
* **Infra-Level-API:**: If this conventions don't fit your needs, you can use our low-level infra API and realize your own conventions.

### Targets

You can define provisioning targets using the [targets-schema](https://github.com/DomainDrivenArchitecture/dda-pallet-commons/blob/master/doc/existing_spec.md)

### Domain API

You can use our conventions as a starting point:
[see domain reference](doc/reference_domain.md)

### Infra API

Or you can build your own conventions using our low level infra API. We will keep this API backward compatible whenever possible:
[see infra reference](doc/reference_infra.md)

## License

Copyright © 2015, 2016, 2017, 2018 meissa GmbH
Licensed under the [Apache License, Version 2.0](LICENSE) (the "License")
Pls. find licenses of our subcomponents [here](doc/SUBCOMPONENT_LICENSE)
