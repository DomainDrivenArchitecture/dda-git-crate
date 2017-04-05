; Copyright (c) meissa GmbH. All rights reserved.
; You must not remove this notice, or any other, from this software.

(ns dda.pallet.domain.dda-git-crate.repo
  (:require
   [clojure.string :as string]
   [clojure.walk :refer (keywordize-keys)]
   [pallet.actions :as actions]
   [pallet.api :as api]
   [schema.core :as s]
   [dda.pallet.crate.dda-git-crate.schema :as schema]
   [dda.pallet.crate.dda-git-crate :as git-crate]
   [dda.pallet.domain.dda-git-crate.parse-url :as pu]))

(s/defn collect-trust :- [schema/ServerTrust]
  [domain-repo-uris :- [s/Str]]
  (let [parsed-uris (map pu/string->url domain-repo-uris)]
    (distinct
      (map (fn [elem] {:pin-fqdn-or-ip (:host elem)}) parsed-uris))))
