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

(ns eva.catalog.common.alpha.transactor-group-test
  (:require [clojure.test :refer :all]
            [clojure.set :as set]
            [eva.catalog.common.alpha.catalog :as catalog]
            [eva.catalog.common.alpha.config :as config]
            [eva.catalog.common.alpha.protocols :refer [get-flat-configs-for-transactor-group]]))


;; Neither of these configs need to be valid


(def broker-test-config (config/activemq-broker "tcp://localhost:5678" "fakeuser" "fakepassword"))
(def storage-test-config (config/sql-storage {}))

;; These tenant, category, and label values
;; DO NOT have any semantic meaning. They are arranged in this way so
;; that we can test various transactor-group assignments.
(def flat-configs-test-data
  (-> (config/empty-catalog)
      (config/add-flat-config "tenant1" "category1" "label1"
                              (config/flat-catalog-config
                               broker-test-config
                               storage-test-config
                               (config/database "storage-partition-1"
                                                "database-1")))
      (config/add-flat-config "tenant2" "category1" "label1"
                              (config/flat-catalog-config
                               broker-test-config
                               storage-test-config
                               (config/database "storage-partition-2"
                                                "database-2")))
      (config/add-flat-config "tenant3" "category1" "label1"
                              (config/flat-catalog-config
                               broker-test-config
                               storage-test-config
                               (config/database "storage-partition-3"
                                                "database-3")))
      (config/add-flat-config "tenant1" "category1" "label2"
                              (config/flat-catalog-config
                               broker-test-config
                               storage-test-config
                               (config/database "storage-partition-1"
                                                "database-4")))
      (config/add-flat-config "tenant2" "category1" "label2"
                              (config/flat-catalog-config
                               broker-test-config
                               storage-test-config
                               (config/database "storage-partition-2"
                                                "database-5")))
      (config/add-flat-config "tenant3" "category1" "label2"
                              (config/flat-catalog-config
                               broker-test-config
                               storage-test-config
                               (config/database "storage-partition-3"
                                                "database-6")))
      (config/add-flat-config "tenant1" "category2" "label3"
                              (config/flat-catalog-config
                               broker-test-config
                               storage-test-config
                               (config/database "storage-partition-1"
                                                "database-7")))
      (config/add-flat-config "tenant2" "category3" "label4"
                              (config/flat-catalog-config
                               broker-test-config
                               storage-test-config
                               (config/database "storage-partition-2"
                                                "database-8")))))

(deftest transactor-group-config-and-lookup
  (testing "without any transactor group assignments, nil is returned for any transactor group besides :unassigned"
    (let [catalog (catalog/data->database-catalog flat-configs-test-data)]
      (are [transactor-group] (nil? (get-flat-configs-for-transactor-group catalog transactor-group))
        "some-transactor-group"
        "any-transactor-group"
        :keyword-transactor-group)
      (is (= (-> catalog :data :flat-configs)
             (get-flat-configs-for-transactor-group catalog :unassigned)))))

  (testing "transactor-group assignments for by tenant"
    (let [catalog (-> flat-configs-test-data
                      (config/assign-transactor-group "transactor-group-for-tenant1" {:tenant "tenant1"})
                      (config/assign-transactor-group "transactor-group-for-tenant2" {:tenant "tenant2"})
                      (catalog/data->database-catalog))]
      (is (contains? (:data catalog) :transactor-groups))
      (is (= #{{::config/transactor-group "transactor-group-for-tenant1"
                ::config/tenant "tenant1"}
               {::config/transactor-group "transactor-group-for-tenant2"
                ::config/tenant "tenant2"}}
             (-> catalog :data :transactor-groups)))

      (are [transactor-group result] (= result (get-flat-configs-for-transactor-group catalog transactor-group))
        "transactor-group-for-tenant1" (set/select #(= "tenant1" (::config/tenant %))
                                                   (:flat-configs flat-configs-test-data))
        "transactor-group-for-tenant2" (set/select #(= "tenant2" (::config/tenant %))
                                                   (:flat-configs flat-configs-test-data))
        "transactor-group-for-tenant3" nil
        :unassigned (set/select #((complement #{"tenant1" "tenant2"})
                                  (::config/tenant %))
                                (:flat-configs flat-configs-test-data)))))

  (testing "transactor-group assignments by category"
    (let [catalog (-> flat-configs-test-data
                      (config/assign-transactor-group "transactor-group-for-category1"
                                                      {:category "category1"})
                      (config/assign-transactor-group "transactor-group-for-category2"
                                                      {:category "category2"})
                      (catalog/data->database-catalog))]
      (are [transactor-group result] (= result (get-flat-configs-for-transactor-group catalog transactor-group))
        "transactor-group-for-category1" (set/select #(= "category1" (::config/category %))
                                                     (:flat-configs flat-configs-test-data))
        "transactor-group-for-category2" (set/select #(= "category2" (::config/category %))
                                                     (:flat-configs flat-configs-test-data))
        "transactor-group-for-category3" nil
        :unassigned (set/select #((complement #{"category1" "category2"})
                                  (::config/category %))
                                (:flat-configs flat-configs-test-data)))))

  (testing "transactor-group assignments by category and label"
    (let [catalog (-> flat-configs-test-data
                      (config/assign-transactor-group "transactor-group1"
                                                      {:category "category1"
                                                       :label "label1"})
                      (config/assign-transactor-group "transactor-group2"
                                                      {:category "category1"
                                                       :label "label2"})
                      (catalog/data->database-catalog))]

      (testing "defined transactor groups"
        (is (= (set/select #(and (= "category1" (::config/category %))
                                 (= "label1" (::config/label %)))
                           (:flat-configs flat-configs-test-data))
               (get-flat-configs-for-transactor-group catalog "transactor-group1")))

        (is (= (set/select #(and (= "category1" (::config/category %))
                                 (= "label2" (::config/label %)))
                           (:flat-configs flat-configs-test-data))
               (get-flat-configs-for-transactor-group catalog "transactor-group2"))))

      (testing "undefined transactor group"
        (is (nil? (get-flat-configs-for-transactor-group catalog "transactor-group3"))))

      (testing "unassigned"
        (is (= (set/select #(or (and (not= "category1" (::config/category %))
                                     (not= "label1" (::config/category %)))
                                (and (not= "category1" (::config/category %))
                                     (not= "label2" (::config/category %))))
                           (:flat-configs flat-configs-test-data))
               (get-flat-configs-for-transactor-group catalog :unassigned)))))))
