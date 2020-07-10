(defproject dda/dda-git-crate "2.2.5-SNAPSHOT"
  :description "Module for cloning and managing git repositories & server conectivity."
  :url "https://domaindrivenarchitecture.org"
  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[dda/dda-pallet "3.1.1"]
                 [dda/dda-serverspec-crate "1.3.4"]]
  :source-paths ["main/src"]
  :resource-paths ["main/resources"]
  :target-path "target/%s/"
  :repositories [["snapshots" :clojars]
                 ["releases" :clojars]]
  :deploy-repositories [["snapshots" :clojars]
                        ["releases" :clojars]]
  :profiles {:dev {:source-paths ["integration/src"
                                  "test/src"
                                  "uberjar/src"]
                   :resource-paths ["integration/resources"
                                    "test/resources"]
                   :dependencies
                   [[org.clojure/test.check "1.0.0"]
                    [dda/pallet "0.9.1" :classifier "tests"]
                    [dda/data-test "0.1.1"]
                    [ch.qos.logback/logback-classic "1.3.0-alpha4"]
                    [org.slf4j/jcl-over-slf4j "2.0.0-alpha1"]]
                   :repl-options {:init-ns dda.pallet.dda-git-crate.app.instantiate-existing}
                   :leiningen/reply {:dependencies [[org.slf4j/jcl-over-slf4j "1.8.0-beta0"
                                                     :exclusions [commons-logging]]]}}
             :test {:test-paths ["test/src"]
                    :resource-paths ["test/resources"]
                    :dependencies [[dda/pallet "0.9.1" :classifier "tests"]
                                   [org.clojure/test.check "1.0.0"]]}
             :uberjar {:source-paths ["uberjar/src"]
                       :resource-paths ["uberjar/resources"]
                       :aot :all
                       :main dda.pallet.dda-git-crate.main
                       :uberjar-name "dda-git-standalone.jar"
                       :dependencies [[org.clojure/tools.cli "1.0.194"]
                                      [ch.qos.logback/logback-classic "1.3.0-alpha4"
                                       :exclusions [com.sun.mail/javax.mail]]
                                      [org.slf4j/jcl-over-slf4j "2.0.0-alpha1"]]}}
  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["deploy"]
                  ["uberjar"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]]
  :local-repo-classpath true)
