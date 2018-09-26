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
    [dda.config.commons.user-home :as user-home]
    [dda.pallet.dda-serverspec-crate.infra :as fact]
    [clojure.pprint :as pp]))

(def Repository
  {:repo s/Str
   :local-dir s/Str
   :settings (hash-set (s/enum :sync))})

(s/defn project-parent-path
  [repo :- Repository]
  (let [{:keys [local-dir]} repo]
    (st/join "/" (drop-last (st/split local-dir #"/")))))

(s/defn path-to-keyword :- s/Keyword
  [path :- s/Str]
  (keyword (st/replace path #"[/]" "_")))

(s/defn
  create-project-parent
  [facility :- s/Keyword
   user-name :- s/Str
   path :- s/Str]
  (actions/as-action
    (logging/info (str facility "-configure user: create-project-parent")))
  (actions/as-action
    (logging/info (str path " " user-name)))
  (actions/exec-checked-script
    "create repo parrent folders"
    ("su" ~user-name "-c" "\"mkdir" "-p" ~path "\"")))

(defn repo-name [repo-uri]
  "Find a repository name from a repo uri string"
  (-> (st/split repo-uri #"/") last (st/replace #"\.git$" "")))

(s/defn clone-repo
  [user-name :- s/Str
   repo :- s/Str
   local-dir :- s/Str]
  (actions/exec-checked-script
   (str "Clone " repo " into " local-dir)
   (if (not (file-exists? ~(str local-dir "/.git/config")))
     ("su" ~user-name "-c" "\"git" "clone" ~repo ~local-dir "\""))))

(s/defn set-url
  [user-name :- s/Str
   repo :- s/Str
   local-dir :- s/Str]
  (actions/exec-checked-script
   (str "Set URL")
   ("cd" ~local-dir)
   ("su" ~user-name "-c" "\"git" "remote" "set-url" "origin" ~repo "\"")))

(s/defn
  clone
  [facility :- s/Keyword
   user-name :- s/Str
   crate-repo :- Repository
   file-fact-keyword :- s/Keyword]
  (let [{:keys [local-dir repo]} crate-repo
        all-facts (crate/get-settings
                          fact/fact-facility
                          {:instance-id (crate/target-node)})
        file-fact (file-fact-keyword all-facts)
        path (path-to-keyword local-dir)]
    (actions/as-action
      (logging/info (str facility "-configure user: clone")))
    (actions/plan-when (:fact-exist? (path (:out @file-fact)))
      (set-url user-name repo local-dir))
    (actions/plan-when (not (:fact-exist? (path (:out @file-fact))))
      (clone-repo user-name repo local-dir))))

(s/defn
  configure-git-sync
  "autosync git repositories"
  [facility :- s/Keyword
   user-name :- s/Str
   repo :- Repository]
  (let [{:keys [local-dir settings]} repo]
    (when (contains? settings :sync)
      (actions/as-action
        (logging/info (str facility "-configure user: configure-git-sync")))
      (actions/remote-file
        (str "/etc/cron.d/90_" (user-home/flatten-user-home-path local-dir))
        :literal true
        :owner "root"
        :group "root"
        :mode "664"
        :content (selmer/render-file
                   "gitsync.templ" {:user-name user-name
                                    :git-repo local-dir})))))


(s/defn configure-user
  "configure user setup"
  [facility :- s/Keyword
   user-name :- s/Str
   repo :- [Repository]
   file-fact-keyword :- s/Keyword]
  (doseq [repo-element repo]
    (let [repo-parent (project-parent-path repo-element)]
      (create-project-parent facility user-name repo-parent)
      (clone facility user-name repo-element file-fact-keyword)
      (configure-git-sync facility user-name repo-element))))
