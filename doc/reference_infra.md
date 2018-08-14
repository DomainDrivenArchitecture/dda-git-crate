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

(def PinElement
 {:host s/Str :port s/Num})

 (def ServerTrust
   {(s/optional-key :pin-fqdn-or-ip) PinElement
    (s/optional-key :fingerprint) s/Str})

(def GitConfig
  {s/Keyword      ; Keyword is user-name
   UserGitConfig})
```
