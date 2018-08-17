```clojure
(def ServerTrust
  {(s/optional-key :pin-fqdn-or-ip) s/Str
   (s/optional-key :fingerprint) s/Str})

(def Repository
 {:repo s/Str
  :local-dir s/Str
  :settings (hash-set (s/enum :sync))})

(def Config {:email s/Str
                       (s/optional-key :signing-key) s/Str
                       (s/optional-key :diff-tool) s/Str})

(def PinElement
 {:host s/Str :port s/Num})

 (def ServerTrust
   {(s/optional-key :pin-fqdn-or-ip) PinElement
    (s/optional-key :fingerprint) s/Str})

(def Config
  {:config Config
   :trust [ServerTrust]
   :repo [Repository]})

(def GitInfra
  {s/Keyword      ; Keyword is user-name
   Config})
```
