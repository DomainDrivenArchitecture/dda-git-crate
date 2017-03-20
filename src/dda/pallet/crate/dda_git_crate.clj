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
(ns dda.pallet.crate.dda-git-crate
  (:require
    [clojure.tools.logging :as logging]
    [schema.core :as s]
    [pallet.api :as api]
    [pallet.actions :as actions]
    [pallet.crate :as crate]
    [org.domaindrivenarchitecture.pallet.core.dda-crate :as dda-crate]
    [org.domaindrivenarchitecture.pallet.servertest.fact.packages :as package-fact]
    [org.domaindrivenarchitecture.pallet.servertest.test.packages :as package-test]))

(def facility :dda-git)
(def version  [0 1 0])

(def GitRepository
  {:ssh-url s/Str
   :https-url s/Str
   :server-type (s/enum :gitblit :github)   
   (s/optional-key :ssh) {(s/optional-key :user) s/Str}
   (s/optional-key :https-public) {(s/optional-key :user) s/Str}
   (s/optional-key :https-private) {:user s/Str
                                    :password s/Str}})

(def GitServerConfig
  {s/Keyword [GitRepository]})

(def GitCrateConfig
  {s/Keyword [GitServerConfig]})

(s/defn git-url :- s/Str 
  [repository :- GitRepository]
  (cond
    (and
      (= :github (get-in repository [:server-type]))
      (contains? repository :ssh)) 
    (str "ssh://git@" (get-in repository [:ssh-url]))
    
    (and
      (= :github (get-in repository [:server-type]))
      (contains? repository :https-public))
    (str "https://" (get-in repository [:https-url]))
    
    (and
      (= :github (get-in repository [:server-type]))
      (contains? repository :https-private))
    (str "https://" (get-in repository [:https-private :user]) ":" (get-in repository [:https-private :password]) "@" (get-in repository [:https-url]))
    
    (and
      (= :gitblit (get-in repository [:server-type]))
      (contains? repository :ssh)
      (not (contains? (get-in repository [:ssh]) :user)))
    (throw (IllegalArgumentException. "For gitblit&ssh an username is required."))
    
    (and
      (= :gitblit (get-in repository [:server-type]))
      (contains? repository :ssh))
    (str "ssh://" (get-in repository [:ssh :user]) "@" (get-in repository [:ssh-url]))
    
    (and
      (= :gitblit (get-in repository [:server-type]))
      (contains? repository :https-public)
      (empty? (get-in repository [:https-public :user])))
    (str "https://" (get-in repository [:https-url]))
    
    (and
      (= :gitblit (get-in repository [:server-type]))
      (contains? repository :https-public)
      (some? (get-in repository [:https-public :user])))
    (str "https://" (get-in repository [:https-public :user]) "@" (get-in repository [:https-url]))
    
    (and
      (= :gitblit (get-in repository [:server-type]))
      (contains? repository :https-private))
    (str "https://" (get-in repository [:https-private :user]) ":" (get-in repository [:https-private :password]) "@" (get-in repository [:https-url]))
    )  
  )


(s/defmethod dda-crate/dda-settings facility
  [dda-crate partial-effective-config]
  ;(package-fact/collect-packages-fact)
  )

(s/defmethod dda-crate/dda-configure facility
  [dda-crate config]
  "dda managed vm: install configure"
  )

(s/defmethod dda-crate/dda-install facility
  [dda-crate config]
  "dda managed vm: install routine"
  )

(s/defmethod dda-crate/dda-test facility
  [dda-crate partial-effective-config]
  )

(def dda-git-crate
  (dda-crate/make-dda-crate
    :facility facility
    :version version))

(def with-git
  (dda-crate/create-server-spec dda-git-crate))
