; Copyright (c) meissa GmbH. All rights reserved.
; You must not remove this notice, or any other, from this software.

(ns dda.pallet.crate.dda-git-crate.instantiate-aws
  (:require
    [clojure.inspector :as inspector]
    [schema.core :as s]
    [pallet.api :as api]
    [pallet.compute :as compute]
    [org.domaindrivenarchitecture.pallet.commons.encrypted-credentials :as crypto]
    [org.domaindrivenarchitecture.pallet.commons.session-tools :as session-tools]
    [org.domaindrivenarchitecture.pallet.commons.pallet-schema :as ps]
    [org.domaindrivenarchitecture.cm.operation :as operation]
    [dda.pallet.domain.dda-git-crate :as domain]))

(defn aws-node-spec []
  (api/node-spec
    :location {:location-id "eu-central-1a"
               ;:location-id "eu-west-1b"
               ;:location-id "us-east-1a"
               }
    :image {:os-family :ubuntu
            ;eu-central-1 16-04 LTS hvm
            :image-id "ami-82cf0aed"
            ;eu-west1 16-04 LTS hvm :image-id "ami-07174474"
            ;us-east-1 16-04 LTS hvm :image-id "ami-45b69e52"
            :os-version "16.04"
            :login-user "ubuntu"}
    :hardware {:hardware-id "t2.micro"}
    :provider {:pallet-ec2 {:key-name "jem"
                            :network-interfaces [{:device-index 0
                                                  :groups ["sg-0606b16e"]
                                                  :subnet-id "subnet-f929df91"
                                                  :associate-public-ip-address true
                                                  :delete-on-termination true}]}}))

(defn aws-provider
  ([]
  (let
    [aws-decrypted-credentials (get-in (pallet.configure/pallet-config) [:services :aws])]
    (compute/instantiate-provider
     :pallet-ec2
     :identity (get-in aws-decrypted-credentials [:account])
     :credential (get-in aws-decrypted-credentials [:secret])
     :endpoint "eu-central-1"
     :subnet-ids ["subnet-f929df91"])))
  ([key-id key-passphrase]
  (let
    [aws-encrypted-credentials (get-in (pallet.configure/pallet-config) [:services :aws])
     aws-decrypted-credentials (crypto/decrypt
                                 (crypto/get-secret-key
                                   {:user-home "/home/mje/"
                                    :key-id key-id})
                                 aws-encrypted-credentials
                                 key-passphrase)]
    (compute/instantiate-provider
     :pallet-ec2
     :identity (get-in aws-decrypted-credentials [:account])
     :credential (get-in aws-decrypted-credentials [:secret])
     :endpoint "eu-central-1"
     :subnet-ids ["subnet-f929df91"]))))

(def domain-config
  {})

(defn converge-install
  ([count]
    (operation/do-converge-install (aws-provider) (domain/dda-git-group count domain-config (aws-node-spec))))
  ([key-id key-passphrase count]
    (operation/do-converge-install (aws-provider key-id key-passphrase) (domain/dda-git-group count domain-config (aws-node-spec))))
  )

(defn server-test
  ([]
    (operation/do-server-test (aws-provider) (domain/dda-git-group domain-config "vmuser")))
  ([key-id key-passphrase]
    (operation/do-server-test (aws-provider key-id key-passphrase) (domain/dda-git-group domain-config "vmuser")))
  )
