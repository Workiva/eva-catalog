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

(ns eva.catalog.server.alpha.handler-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [ring.mock.request :as mock]
            [eva.catalog.server.alpha.handler :refer :all]
            [eva.catalog.common.alpha.config :as config]
            [eva.catalog.common.alpha.catalog :as c]
            [eva.catalog.common.alpha.utils :refer [*env-overrides* env]]))

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

(defn test-app [catalog]
  (let [app (build-routes catalog)]

    (testing "status route"
      (let [response (app (mock/request :get "/status"))]
        (is (= (:status response) 200))
        (is (= (:body response) "All systems nominal."))))

    (testing "flat-peer-config route"
      (let [happy-response (app
                            (-> (mock/request :get "/flat-peer-config"
                                              {:tenant "workiva"
                                               :category "eva-test-1"
                                               :label "test-db-1"})
                                (mock/content-type "application/edn")
                                (mock/header :accept "application/edn")))

            sad-response
            (app (-> (mock/request :get "/flat-peer-config" {})
                     (mock/content-type "application/edn")))]

        (is (= (:status happy-response) 200))
        (is (= (dissoc (read-string (slurp (:body happy-response)))
                       :eva.v2.system.peer-connection.core/id)
               test-db-1-config))

        (is (= (:status sad-response) 404))))

    (testing "flat-peer-configs-for-tenant-and-category route"
      (let [happy-response (app
                            (-> (mock/request :get "/flat-peer-configs-for-tenant-and-category"
                                              {:tenant "workiva"
                                               :category "eva-test-2"})
                                (mock/content-type "application/edn")
                                (mock/header :accept "application/edn")))

            sad-response
            (app (-> (mock/request :get "/flat-peer-configs-for-tenant-and-category"
                                   {:tenant "NOT-workiva"
                                    :category "eva-test-2"})
                     (mock/content-type "application/edn")))]

        (is (= (:status happy-response) 200))
        (is (= (into #{} (read-string (slurp (:body happy-response))))
               eva-test-2-labels))

        (is (= (:status sad-response) 404))))

    (testing "flat-peer-configs-for-category-and-label route"
      (let [happy-response (app
                            (-> (mock/request :get "/flat-peer-configs-for-category-and-label"
                                              {:category "eva-test-2"
                                               :label "test-db-4"})
                                (mock/content-type "application/edn")
                                (mock/header :accept "application/edn")))

            sad-response
            (app (-> (mock/request :get "/flat-peer-configs-for-tenant-and-category"
                                   {:tenant "NOT-workiva"
                                    :category "eva-test-2"})
                     (mock/content-type "application/edn")))]

        (is (= (:status happy-response) 200))
        (is (= (into #{} (read-string (slurp (:body happy-response))))
               #{test-db-4-config}))

        (is (= (:status sad-response) 404))))

    (testing "not-found route"
      (let [response (app (mock/request :get "/invalid"))]
        (is (= (:status response) 404))))))

(deftest test-edn-file-catalog
  (test-app (c/load-catalog {::c/type :edn-file :path (io/resource "eva/catalog/common/alpha/mock-catalog-data.edn")})))
