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

(ns eva.catalog.client.alpha.client-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [ring.adapter.jetty :as raj]
            [eva.catalog.common.alpha.catalog :as c]
            [eva.catalog.client.alpha.client :as client]
            [eva.catalog.common.alpha.config :as config]
            [eva.catalog.common.alpha.utils :refer [*env-overrides* env]]
            [eva.catalog.server.alpha.handler :refer :all])
  (:import [eva.catalog.client.alpha HTTPCatalogClientImpl]))

(def test-db-1-config
  {::config/tenant "workiva"
   ::config/category "eva-test-1"
   ::config/label "test-db-1"

   :messenger-node-config/type :broker-uri,
   :broker-uri
   "tcp://activemq:61616?user=eva-transactor&password=notasecret&retryInterval=1000&retryIntervalMultiplier=2.0&maxRetryInterval=60000&reconnectAttempts=-1",
   :eva.v2.storage.block-store.types/storage-type
   :eva.v2.storage.block-store.types/sql,
   :eva.v2.storage.block-store.impl.sql/db-spec
   {:subprotocol "mysql",
    :subname "//localhost:3306/eva",
    :classname "com.mysql.jdbc.Driver",
    :user "sa",
    :password "notasecret"},
   :eva.v2.storage.value-store.core/partition-id
   #uuid "63621540-10fc-402a-b305-97f312d0f8f0",
   :eva.v2.database.core/id
   #uuid "cad72d3a-3af7-4111-9d68-03c75102486d",
   :eva.v2.messaging.address/transaction-submission
   "eva.v2.transact.63621540-10fc-402a-b305-97f312d0f8f0.cad72d3a-3af7-4111-9d68-03c75102486d",
   :eva.v2.messaging.address/transaction-publication
   "eva.v2.transacted.63621540-10fc-402a-b305-97f312d0f8f0.cad72d3a-3af7-4111-9d68-03c75102486d",
   :eva.v2.messaging.address/index-updates
   "eva.v2.index-updates.63621540-10fc-402a-b305-97f312d0f8f0.cad72d3a-3af7-4111-9d68-03c75102486d"})

(def test-db-2-config
  {::config/tenant "workiva"
   ::config/category "eva-test-1"
   ::config/label "test-db-2"

   :messenger-node-config/type :broker-uri,
   :broker-uri
   "tcp://activemq:61616?user=eva-transactor&password=notasecret&retryInterval=1000&retryIntervalMultiplier=2.0&maxRetryInterval=60000&reconnectAttempts=-1",
   :eva.v2.storage.block-store.types/storage-type
   :eva.v2.storage.block-store.types/sql,
   :eva.v2.storage.block-store.impl.sql/db-spec
   {:subprotocol "mysql",
    :subname "//localhost:3306/eva",
    :classname "com.mysql.jdbc.Driver",
    :user "sa",
    :password "notasecret"},
   :eva.v2.storage.value-store.core/partition-id
   #uuid "63621540-10fc-402a-b305-97f312d0f8f0",
   :eva.v2.database.core/id
   #uuid"b3923f98-cae5-4503-a572-f19a1f261fb0",
   :eva.v2.messaging.address/transaction-submission
   "eva.v2.transact.63621540-10fc-402a-b305-97f312d0f8f0.b3923f98-cae5-4503-a572-f19a1f261fb0",
   :eva.v2.messaging.address/transaction-publication
   "eva.v2.transacted.63621540-10fc-402a-b305-97f312d0f8f0.b3923f98-cae5-4503-a572-f19a1f261fb0",
   :eva.v2.messaging.address/index-updates
   "eva.v2.index-updates.63621540-10fc-402a-b305-97f312d0f8f0.b3923f98-cae5-4503-a572-f19a1f261fb0"})

(def test-db-3-config
  {::config/tenant "workiva"
   ::config/category "eva-test-2"
   ::config/label "test-db-3"

   :eva.v2.messaging.address/transaction-publication
   "eva.v2.transacted.63621540-10fc-402a-b305-97f312d0f8f0.eaf51494-0032-4381-971a-7946b012724c",
   :eva.v2.messaging.address/transaction-submission
   "eva.v2.transact.63621540-10fc-402a-b305-97f312d0f8f0.eaf51494-0032-4381-971a-7946b012724c",
   :eva.v2.storage.block-store.impl.sql/db-spec
   {:subprotocol "mysql",
    :subname "//localhost:3306/eva",
    :classname "com.mysql.jdbc.Driver",
    :user "sa",
    :password "notasecret"},
   :eva.v2.database.core/id
   #uuid "eaf51494-0032-4381-971a-7946b012724c",
   :eva.v2.storage.block-store.types/storage-type
   :eva.v2.storage.block-store.types/sql,
   :eva.v2.messaging.address/index-updates
   "eva.v2.index-updates.63621540-10fc-402a-b305-97f312d0f8f0.eaf51494-0032-4381-971a-7946b012724c",
   :eva.v2.storage.value-store.core/partition-id
   #uuid "63621540-10fc-402a-b305-97f312d0f8f0",
   :broker-uri
   "tcp://activemq:61616?user=eva-transactor&password=notasecret&retryInterval=1000&retryIntervalMultiplier=2.0&maxRetryInterval=60000&reconnectAttempts=-1",
   :messenger-node-config/type :broker-uri})

(def test-db-4-config
  {::config/tenant "workiva"
   ::config/category "eva-test-2"
   ::config/label "test-db-4"

   :eva.v2.messaging.address/transaction-publication
   "eva.v2.transacted.63621540-10fc-402a-b305-97f312d0f8f0.f998bc77-c090-4c3a-b86e-1b063536bcf3",
   :eva.v2.messaging.address/transaction-submission
   "eva.v2.transact.63621540-10fc-402a-b305-97f312d0f8f0.f998bc77-c090-4c3a-b86e-1b063536bcf3",
   :eva.v2.storage.block-store.impl.sql/db-spec
   {:subprotocol "mysql",
    :subname "//localhost:3306/eva",
    :classname "com.mysql.jdbc.Driver",
    :user "sa",
    :password "notasecret"},
   :eva.v2.database.core/id
   #uuid "f998bc77-c090-4c3a-b86e-1b063536bcf3",
   :eva.v2.storage.block-store.types/storage-type
   :eva.v2.storage.block-store.types/sql,
   :eva.v2.messaging.address/index-updates
   "eva.v2.index-updates.63621540-10fc-402a-b305-97f312d0f8f0.f998bc77-c090-4c3a-b86e-1b063536bcf3",
   :eva.v2.storage.value-store.core/partition-id
   #uuid "63621540-10fc-402a-b305-97f312d0f8f0",
   :broker-uri
   "tcp://activemq:61616?user=eva-transactor&password=notasecret&retryInterval=1000&retryIntervalMultiplier=2.0&maxRetryInterval=60000&reconnectAttempts=-1",
   :messenger-node-config/type :broker-uri})

(def test-transactor-storage
  (config/sql-storage {:subprotocol (env "EVA_STORAGE_SQL_DB_TYPE" "mysql")
                       :subname     (format "//%s:%s/%s"
                                            (env "EVA_STORAGE_SQL_HOST" "localhost")
                                            (env "EVA_STORAGE_SQL_PORT" 3306)
                                            (env "EVA_STORAGE_SQL_DBNAME" "eva"))
                       :classname   (env "EVA_STORAGE_SQL_DRIVER" "com.mysql.jdbc.Driver")
                       :user (env "EVA_STORAGE_SQL_USER" "special-transactor-user")
                       :password (env "EVA_STORAGE_SQL_PASSWORD" "special-transactor-password")}))

(def eva-test-2-labels
  #{test-db-3-config test-db-4-config})

;; Lifted from ring.adapter.jetty testing
;; https://github.com/ring-clojure/ring/blob/master/ring-jetty-adapter/test/ring/adapter/test/jetty.clj
(defmacro with-server [app options & body]
  `(let [server# (raj/run-jetty ~app ~(assoc options :join? false))]
     (try
       ~@body
       (finally (.stop server#)))))

(defn test-txor-config [peer-cfg txor-cfg]
  (-> (merge peer-cfg txor-cfg (client/indexer-id-map) (client/transactor-id-map))
      (dissoc client/peer-id-key)))

(def mock-catalog-data-path (-> (io/resource "eva/catalog/common/alpha/mock-catalog-data.edn")
                                (io/file)
                                (.getAbsolutePath)))

(def mock-transactor-data-path (-> (io/resource "eva/catalog/client/alpha/mock-transactor-data.edn")
                                   (io/file)
                                   (.getAbsolutePath)))

(deftest full-stack-test
  (let [catalog (c/load-catalog {::c/type :edn-file :path mock-catalog-data-path})
        routes (build-routes catalog)]
    (with-server routes {:host "0.0.0.0" :port 3001}
      (is (= test-db-1-config
             (dissoc
              (HTTPCatalogClientImpl/requestFlatPeerConfig "http://localhost:3001" "workiva" "eva-test-1" "test-db-1")
              :eva.v2.system.peer-connection.core/id)
             (binding [*env-overrides* {"EVA_LOCAL_CATALOG_DATA" mock-catalog-data-path}]
               (dissoc
                (HTTPCatalogClientImpl/requestFlatPeerConfig "LOCAL" "workiva" "eva-test-1" "test-db-1")
                :eva.v2.system.peer-connection.core/id))))

      (is (= eva-test-2-labels
             (->>
              (HTTPCatalogClientImpl/requestFlatPeerConfigsForTenantAndCategory "http://localhost:3001"
                                                                                "workiva"
                                                                                "eva-test-2")

              (map (fn [config] (dissoc config :eva.v2.system.peer-connection.core/id)))
              (into #{}))
             (binding [*env-overrides* {"EVA_LOCAL_CATALOG_DATA" mock-catalog-data-path}]
               (->>
                (HTTPCatalogClientImpl/requestFlatPeerConfigsForTenantAndCategory "LOCAL"
                                                                                  "workiva"
                                                                                  "eva-test-2")
                (map (fn [config] (dissoc config :eva.v2.system.peer-connection.core/id)))
                (into #{})))))

      (is (= #{test-db-3-config}
             (->>
              (HTTPCatalogClientImpl/requestFlatPeerConfigsForCategoryAndLabel "http://localhost:3001"
                                                                               "eva-test-2"
                                                                               "test-db-3")

              (map (fn [config] (dissoc config :eva.v2.system.peer-connection.core/id)))
              (into #{}))
             (binding [*env-overrides* {"EVA_LOCAL_CATALOG_DATA" mock-catalog-data-path}]
               (->>
                (HTTPCatalogClientImpl/requestFlatPeerConfigsForCategoryAndLabel "LOCAL"
                                                                                 "eva-test-2"
                                                                                 "test-db-3")
                (map (fn [config] (dissoc config :eva.v2.system.peer-connection.core/id)))
                (into #{})))))
      (binding [*env-overrides* {"EVA_LOCAL_CATALOG_DATA" mock-transactor-data-path}]

        (is (= (test-txor-config test-db-1-config test-transactor-storage)
               (client/request-flat-transactor-config "http://localhost:3001" "workiva" "eva-test-1" "test-db-1")))

        (is (= #{(test-txor-config test-db-3-config test-transactor-storage)
                 (test-txor-config test-db-4-config test-transactor-storage)}
               (client/request-flat-transactor-configs-for-tenant-and-category
                "http://localhost:3001" "workiva" "eva-test-2")))

        (is (= #{(test-txor-config test-db-3-config test-transactor-storage)}
               (client/request-flat-transactor-configs-for-category-and-label
                "http://localhost:3001" "eva-test-2" "test-db-3"))))

      (testing "transactor group configs lookup"
        (is (= #{test-db-1-config
                 test-db-2-config}
               (HTTPCatalogClientImpl/requestFlatConfigsInTransactorGroup
                "http://localhost:3001"
                "category:eva-test-1")))

        (is (= #{test-db-3-config}
               (HTTPCatalogClientImpl/requestFlatConfigsInTransactorGroup
                "http://localhost:3001"
                "category:eva-test-2;label:test-db-3")))

        (is (= #{test-db-4-config}
               (HTTPCatalogClientImpl/requestFlatConfigsWithoutTransactorGroup
                "http://localhost:3001")))))))
