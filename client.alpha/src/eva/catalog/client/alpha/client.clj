;; Copyright 2018-2019 Workiva Inc.
;; 
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;; 
;;     http://www.apache.org/licenses/LICENSE-2.0
;; 
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns eva.catalog.client.alpha.client
  "A local client, intended to be produced as a library artifact for import."
  (:require [eva.catalog.common.alpha.utils :refer [required-env
                                                    load-edn-data-from-file
                                                    join]]
            [eva.catalog.common.alpha.config :as config]
            [eva.catalog.common.alpha.catalog :as c]
            [eva.catalog.common.alpha.protocols :as p]
            [clojure.edn :as edn]
            [clojure.string :as cstr]
            [clj-http.lite.client :as http-client]
            [slingshot.slingshot :as sl])
  (:import [java.util UUID]
           [java.net URI]))

(def peer-id (memoize #(java.util.UUID/randomUUID)))
(def transactor-id (memoize #(java.util.UUID/randomUUID)))
(def indexer-id (memoize #(java.util.UUID/randomUUID)))

(def peer-id-key :eva.v2.system.peer-connection.core/id)
(def transactor-id-key :eva.v2.system.transactor.core/id)
(def indexer-id-key :eva.v2.system.indexing.core/id)

(defn peer-id-map [] {peer-id-key (peer-id)})
(defn transactor-id-map [] {transactor-id-key (transactor-id)})
(defn indexer-id-map [] {indexer-id-key (indexer-id)})

(def local-catalog "LOCAL")

(defn not-found-error [data]
  (ex-info "no config(s) found"
           (merge {:error ::not-found}
                  data)))

(defmacro def-http-requestor
  "Macro to construct function that sends a synchronous http request to the
   provided endpoint with the provided query args."
  [fn-name endpoint args]
  (assert (simple-symbol? fn-name) "fn-name must be a simple symbol")
  (assert (and (string? endpoint) (cstr/starts-with? endpoint "/")))
  (assert (every? simple-symbol? args) "Args must be simple symbols")
  (let [arg-map (into {} (for [arg args] [(keyword arg) arg]))
        catalog-address (gensym 'catalog-address)
        fn-args (into [catalog-address] args)]
    `(defn ~fn-name ~fn-args
       (sl/try+ (-> (str ~catalog-address ~endpoint)
                    (http-client/get {:accept "application/edn"
                                      :query-params ~arg-map})
                    :body
                    (edn/read-string))
                (catch [:status 404] e#
                  (throw (not-found-error ~arg-map)))))))

(def-http-requestor request-http-config
  "/flat-peer-config"
  [tenant category label])
(def-http-requestor request-http-configs-for-tenant-and-category
  "/flat-peer-configs-for-tenant-and-category"
  [tenant category])
(def-http-requestor request-http-configs-for-category-and-label
  "/flat-peer-configs-for-category-and-label"
  [category label])
(def-http-requestor request-http-configs-for-transactor-group
  "/flat-transactor-group-configs"
  [transactor-group])

(defn resolve-locally
  "Given an f as a function on a database catalog, load the local catalog state
   and evaluate f against it with the provided args."
  [f & args]
  (let [catalog (c/load-catalog {::c/type :env-var :env-var "EVA_LOCAL_CATALOG_DATA"})]
    (apply f catalog args)))

(defn request-flat-config [catalog-address
                           tenant
                           category
                           label]
  (-> (cond
        (= catalog-address local-catalog)
        (or (resolve-locally p/get-flat-config tenant category label :peer)
            (throw (not-found-error {:catalog-address catalog-address
                                     :tenant tenant
                                     :category category
                                     :label label})))

        (string? catalog-address)
        (request-http-config catalog-address tenant category label)

        :else
        (throw (IllegalArgumentException. "Unexpected argument to flat config, expected \"LOCAL\" or uri string.")))
      (merge (peer-id-map))))

(defn -requestFlatPeerConfig [catalog-address tenant category label]
  (request-flat-config catalog-address tenant category label))

(defn request-flat-configs-for-tenant-and-category [catalog-address
                                                    tenant
                                                    category]
  (->> (cond
         (= catalog-address local-catalog)
         (or (resolve-locally p/get-flat-configs-for-tenant-and-category tenant category :peer)
             (throw (not-found-error {:catalog-address catalog-address
                                      :tenant tenant
                                      :category category})))

         (string? catalog-address)
         (request-http-configs-for-tenant-and-category catalog-address tenant category)

         :else
         (throw (IllegalArgumentException. "Unexpected argument to flat config, expected \"LOCAL\" or uri string.")))
       (into #{} (map #(merge % (peer-id-map))))))

(defn -requestFlatPeerConfigsForTenantAndCategory [catalog-address tenant category]
  (request-flat-configs-for-tenant-and-category catalog-address tenant category))

(defn request-flat-configs-for-category-and-label [catalog-address
                                                   category
                                                   label]
  (->> (cond
         (= catalog-address local-catalog)
         (or (resolve-locally p/get-flat-configs-for-category-and-label category label :peer)
             (throw (not-found-error {:catalog-address catalog-address
                                      :label label})))

         (string? catalog-address)
         (request-http-configs-for-category-and-label catalog-address category label)

         :else
         (throw (IllegalArgumentException. "Unexpected argument to flat config, expected \"LOCAL\" or uri string.")))
       (into #{} (map #(merge % (peer-id-map))))))

(defn -requestFlatPeerConfigsForCategoryAndLabel [catalog-address category label]
  (request-flat-configs-for-category-and-label catalog-address category label))





;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Transactor-specific end points ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; The following allows splitting / merging of configuration files in order to
;; let the transactor reuse most of the same data that's provided by the peer
;; endpoints in a deployed catalog, while still having its own locally deployed
;; info that will overwrite the remote info. Only provided clj-side hooks, since
;; we're probably the only ones using this once it's integrated into Eva.

;; It's written generically, so whatever fields we want to split apart we can --
;; it just relies on the transactor having its own local version of the (submap)
;; of catalog info it's supposed to overwrite.
;; I assumed that the primary concern is database credentials, so I wrote the
;; tests to mimic this behavior (note the storage here uses its own username +
;; password, distinct from the ones in the config used by the test server)
;; but something similar could be done for, eg, messaging credentials

;; (it does this by performing a natural join on the 'primary keys' of the
;; remote catalog 'relation' and the local catalog 'relation', where any locally
;; joined maps are merged over the remote maps -- so all you need to do is have
;; a shared tenant, category, and label on the remote and local configs)


(def primary-keys [::config/tenant ::config/category ::config/label])

(def primary-keys-join-map (zipmap primary-keys primary-keys))

(defn request-flat-transactor-config
  "Requests a peer configuration from the provided catalog, then uses the local
   catalog to look up and merge transactor-specific information onto the config"
  [catalog-address tenant category label]
  (let [peer-cfg (request-flat-config catalog-address tenant category label)
        local-cfg (request-flat-config local-catalog tenant category label)]
    (-> peer-cfg
        (merge local-cfg (transactor-id-map) (indexer-id-map))
        (dissoc peer-id-key))))

(defn request-flat-transactor-configs-for-tenant-and-category
  "Requests peer configurations from the provided catalog, then uses the local
   catalog to look up and merge transactor-specific information onto the config"
  [catalog-address tenant category]
  (let [peer-cfgs (request-flat-configs-for-tenant-and-category catalog-address tenant category)
        local-cfgs (request-flat-configs-for-tenant-and-category local-catalog tenant category)]
    (->> (join peer-cfgs local-cfgs primary-keys-join-map)
         (map #(merge % (transactor-id-map) (indexer-id-map)))
         (map #(dissoc % peer-id-key))
         (into #{}))))

(defn request-flat-transactor-configs-for-category-and-label
  "Requests peer configurations from the provided catalog, then uses the local
   catalog to look up and merge transactor-specific information onto the config"
  [catalog-address category label]
  (let [peer-cfgs (request-flat-configs-for-category-and-label catalog-address category label)
        local-cfgs (request-flat-configs-for-category-and-label local-catalog category label)]
    (->> (join peer-cfgs local-cfgs primary-keys-join-map)
         (map #(merge % (transactor-id-map) (indexer-id-map)))
         (map #(dissoc % peer-id-key))
         (into #{}))))


;; Transactor Group Calls


(defn request-flat-configs-in-transactor-group
  [catalog-address transactor-group]
  (into #{}
        (cond
          (= catalog-address local-catalog)
          (or (resolve-locally p/get-flat-configs-for-transactor-group transactor-group)
              (throw (not-found-error {:transactor-group transactor-group})))

          (string? catalog-address)
          (request-http-configs-for-transactor-group catalog-address transactor-group)

          :else
          (throw (IllegalArgumentException. "Unexpected argument to flat config, expected \"LOCAL\" or uri string.")))))

(def -requestFlatConfigsInTransactorGroup request-flat-configs-in-transactor-group)

(def ^:private no-transactor-group (str :unassigned))

(defn request-flat-configs-without-transactor-group
  [catalog-address]
  (request-flat-configs-in-transactor-group catalog-address no-transactor-group))

(def -requestFlatConfigsWithoutTransactorGroup request-flat-configs-without-transactor-group)

