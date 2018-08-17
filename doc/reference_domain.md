```clojure
(def ServerIdentity
  {:host s/Str                                 ;identifyer for repo matching
   (s/optional-key :port) s/Num                ;identifyer for repo matching, defaults to 22 or 443 based on access-type
   :access-type (s/enum :ssh :https)})

(def Repository
  (merge
    ServerIdentity
    {(s/optional-key :orga-path) s/Str
     :repo-name s/Str
     :server-type (s/enum :gitblit :github :gitlab)}))

(def GitCredentials
  [(merge
     ServerIdentity
     {(s/optional-key :user-name) secret/Secret    ;needed for none-public access
      (s/optional-key :password) secret/Secret})]) ;needed for none-public & none-key access

(def GitInfra
  {:user-email s/Str
   (s/optional-key :signing-key) s/Str
   (s/optional-key :diff-tool) s/Str
   (s/optional-key :credentials) GitCredentials
   (s/optional-key :repos) {s/Keyword [s/Str]}
   (s/optional-key :synced-repos) {s/Keyword [s/Str]}})

(def GitDomain
  {s/Keyword                 ;represents the user-name
   GitInfra})
```
