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
(ns dda.pallet.dda-git-crate.infra.git-repo
  (:require
    [clojure.tools.logging :as logging]
    [clojure.string :as st]
    [schema.core :as s]
    [pallet.actions :as actions]
    [pallet.crate :as crate]
    [selmer.parser :as selmer]
    [dda.config.commons.user-home :as user-home]))

(def GitRepository
  {:repo s/Str
   :local-dir s/Str
   :settings (hash-set (s/enum :sync))})

(s/defn
  project-parent-path
  [repo :- GitRepository]
  (let [{:keys [local-dir]} repo]
    (st/join "/" (drop-last (st/split local-dir #"/")))))

(s/defn
  create-project-parent
  [path :- s/Str]
  (actions/directory path))

(defn repo-name [repo-uri]
  "Find a repository name from a repo uri string"
  (-> (st/split repo-uri #"/") last (st/replace #"\.git$" "")))

(crate/defplan clone
  "Clone a repository from `repo-uri`, a uri string.
By default the `:checkout-dir` option is found from the `repo-uri`.
`:args` can be used to pass a sequence of arbitrary arguments to the
clone command."
  [repo-uri & {:keys [checkout-dir args]
               :or {checkout-dir (repo-name repo-uri)}
               :as options}]
  (actions/exec-checked-script
   (str "Clone " repo-uri " into " checkout-dir)
   (if (not (file-exists? ~(str checkout-dir "/.git/config")))
     ("git" clone ~@(or (seq args) [""]) ~repo-uri ~checkout-dir))))

(s/defn
  clone
  [crate-repo :- GitRepository]
  (let [{:keys [local-dir repo]} crate-repo]
    (clone
      repo
      :checkout-dir local-dir)))

(s/defn
  configure-git-sync
  "autosync git repositories"
  [user :- s/Str
   repo :- GitRepository]
  (let [{:keys [local-dir settings]} repo]
    (when (contains? settings :sync)
      (actions/remote-file
        (str "/etc/cron.d/90_" (user-home/flatten-user-home-path local-dir))
        :literal true
        :owner "root"
        :group "root"
        :mode "664"
        :content (selmer/render-file
                   "gitsync.templ" {:user-name user
                                    :git-repo local-dir})))))
