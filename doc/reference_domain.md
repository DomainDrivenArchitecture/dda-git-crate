```clojure
(def ServerIdentity
  {:host s/Str                                 ;identifyer for repo matching
   (s/optional-key :port) s/Num                ;identifyer for repo matching, defaults to 22 or 443 based on protocol
   :protocol (s/enum :ssh :https)})

(def Repository
  (merge
    ServerIdentity
    {(s/optional-key :orga-path) s/Str
     :repo-name s/Str
     :server-type (s/enum :gitblit :github :gitlab)}))

(def OrganizedRepositories {s/Keyword [Repository]})

(def GitCredential
  (merge
     ServerIdentity
     {:user-name secret/Secret                    ;needed for none-public access
      (s/optional-key :password) secret/Secret})) ;needed for none-public & none-key access

(def GitCredentials [GitCredential])

(def UserGit
  {:user-email s/Str
   (s/optional-key :signing-key) s/Str
   (s/optional-key :diff-tool) s/Str
   (s/optional-key :credential) repo/GitCredentials
   (s/optional-key :repo) repo/OrganizedRepositories
   (s/optional-key :synced-repo) repo/OrganizedRepositories})

(def GitDomain
  {s/Keyword                 ;represents the user-name
   UserGit})
```
