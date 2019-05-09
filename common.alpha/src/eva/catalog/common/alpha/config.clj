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

(ns eva.catalog.common.alpha.config
  "Namespace for evaluating configuration files in. Contains helper functions
   which make evaluating and expanding configurations simpler."
  (:require [eva.catalog.common.alpha.utils :refer [env required-env]])
  (:import [java.io PushbackReader]))

(defn broker-uri [uri] {:messenger-node-config/type :broker-uri, :broker-uri uri})

(defn activemq-broker [uri user password]
  {:messenger-node-config/type :broker-uri,
   :broker-uri uri
   :broker-type "org.apache.activemq.ActiveMQConnectionFactory"
   :broker-user user
   :broker-password password})

(defn sql-storage [db-spec]
  {:eva.v2.storage.block-store.types/storage-type :eva.v2.storage.block-store.types/sql
   :eva.v2.storage.block-store.impl.sql/db-spec db-spec})

(defn database [partition-id database-id]
  {:eva.v2.storage.value-store.core/partition-id partition-id
   :eva.v2.database.core/id                      database-id})

(defn address [type database-conf]
  (format "eva.v2.%s.%s.%s"
          (name type)
          (:eva.v2.storage.value-store.core/partition-id database-conf)
          (:eva.v2.database.core/id database-conf)))

(defn empty-catalog [] {:flat-configs #{}})

(defn flat-catalog-config [broker-conf
                           storage-conf
                           {:as database-conf :keys [:eva.v2.storage.value-store.core/partition-id
                                                     :eva.v2.database.core/id]}]
  (let [tc (merge broker-conf
                  storage-conf
                  database-conf
                  {:eva.v2.messaging.address/transaction-submission  (address "transact" database-conf)
                   :eva.v2.messaging.address/transaction-publication (address "transacted" database-conf)
                   :eva.v2.messaging.address/index-updates           (address "index-updates" database-conf)})]
    tc))

(defn add-flat-config [catalog tenant category label config]
  (update catalog :flat-configs conj (merge config {::tenant tenant ::category category ::label label})))

(defn assign-transactor-group
  [catalog
   transactor-group
   {:keys [:category :label :tenant]}]
  (assert (not= :unassigned transactor-group)
          ":unassigned is reserved and cannot be used as a transactor-group")
  (assert (not= (str :unassigned) transactor-group)
          ":unassigned is reserved and cannot be used as a transactor-group")
  (assert (or category label tenant)
          "must provide at least one of category, label, or tenant")
  (update catalog :transactor-groups
          (fnil conj #{})
          (-> {}
              (assoc ::transactor-group transactor-group)
              (cond->
               category (assoc ::category category)
               label (assoc ::label label)
               tenant (assoc ::tenant tenant)))))
