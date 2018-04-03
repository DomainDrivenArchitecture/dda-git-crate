; Copyright (c) meissa GmbH. All rights reserved.
; You must not remove this notice, or any other, from this software.

(ns dda.pallet.dda-git-crate.domain.parse-url
  (:require
   [clojure.string :as string]
   [clojure.walk :refer (keywordize-keys)]
   [pallet.actions :as actions]
   [pallet.api :as api]
   [schema.core :as s]
   [dda.pallet.dda-git-crate.infra :as git-crate]))

(defn- maybe
  "Helper function for creating predicates that might be nil."
  [pred]
  (some-fn nil? pred))

(defrecord Url [scheme user host port path query fragment])

(defn url? [x] (instance? Url x))

(defn make-url
  "Convenience constructor for Url."
  [& {:keys [scheme user host port path query fragment]
      :or {path []
           query {}}}]
  (->Url scheme user host port path query fragment))

(def ^:private url-pattern
  #"^(?:([^:/?#]+):)?(?://([^/?#]*))?([^?#]*)(?:\?([^#]*))?(?:#(.*))?")

(def ^:private host-pattern
  #"^(?:([^@]*)@)?(.+?)(?::(\d+))?$")

(defn- path-string->list
  "Converts a path string (which may be nil) to a vector of path elements."
  [path]
  {:pre  [((maybe string?) path)]
   :post [(vector? %) (every? string? %)]}
  ; drop the first element of the path because it contains a leading slash
  (into [] (and path (rest (string/split path #"/")))))

(defn- query-map?
  "A predicate for determining if a map is a valid representation of a query string."
  [query]
  {:pre [(map? query)]}
  (every?
    (fn [[k v]]
      (and (keyword? k)
           ((maybe string?) v)))
    query))

(defn- query-string->map
  "Converts a query string (which may be nil) to a map representation."
  [query]
  {:pre  [((maybe string?) query)]
   :post [(map? %) (query-map? %)]}
  (if-not query
    {}
    (let [elements (string/split query #"&")
          pairs (map #(string/split % #"=" 2) elements)]
      (keywordize-keys  (into {} ; this is necessary when v is nil
                              (for [[k v] pairs] [k v]))))))

(defn- query-map->string
  "Converts a map representation of a query string to a string."
  [query]
  {:pre  [(query-map? query)]
   :post [(string? %)]}
  (string/join
    "&"
    (for [[k v] query]
      (str (name k) "=" v))))

(defn string->url
  "Parses a string into a url. Malformed or incomplete urls are supported,
  and the relevant fields will be left nil."
  [string]
  {:pre  [(string? string)]
   :post [(url? %)]}
  (let [[_ scheme user+host+port path query fragment]
        (re-matches url-pattern string)
        [_ user host port]
        (if user+host+port (re-matches host-pattern user+host+port) [])]
    (->Url (some-> scheme string/lower-case)
           user
           (some-> host string/lower-case)
           (some-> port Integer/parseInt)
           (path-string->list path)
           (query-string->map query) fragment)))

(defn url->string
  "Gets the string representation of a url. Missing portions are not included
  in the result."
  [url]
  {:pre  [(url? url)]
   :post [(string? %)]}
  (let [{:keys [scheme user host port path query fragment]} url]
    (str
      (some-> scheme (str "://"))
      (some-> user (str "@"))
      host
      (some->> port (str ":"))
      (when-not (empty? path) (str "/" (string/join "/" path)))
      (when-not (empty? query) (str "?" (query-map->string query)))
      (some->> fragment (str "#")))))
