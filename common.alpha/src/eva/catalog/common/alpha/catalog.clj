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

(ns eva.catalog.common.alpha.catalog
  (:require [eva.catalog.common.alpha.protocols :as p]
            [eva.catalog.common.alpha.config :as config]
            [eva.catalog.common.alpha.utils :refer [required-env]]
            [clojure.set :as set :refer [select]]
            [clojure.java.io :as io]
            [clojure.edn :as edn])
  (:import [java.io PushbackReader]
           [java.util UUID]))

(defn load-catalog-data-from-file [source]
  (binding [*read-eval* false
            *ns* (the-ns 'eva.catalog.common.alpha.config)]
    (with-open [r (PushbackReader. (io/reader source))]
      (eval (read r)))))

(defmacro ^:private spy [form]
  `(let [result# ~form]
     (println (str (pr-str '~form) " => " (pr-str result#)))
     result#))

(defrecord DatabaseCatalog [data]
  p/DatabaseCatalogRead
  (get-flat-config [_ tenant category label role]
    (->> (:flat-configs data)
         (select (fn [config]
                   (and (= tenant   (::config/tenant config))
                        (= category (::config/category config))
                        (= label    (::config/label config)))))
         first))

  (get-flat-configs-for-tenant-and-category [_ tenant category role]
    (->> (:flat-configs data)
         (select (fn [config]
                   (and (= tenant (::config/tenant config))
                        (= category (::config/category config)))))
         not-empty))

  (get-flat-configs-for-category-and-label [_ category label role]
    (->> (:flat-configs data)
         (select (fn [config]
                   (and (= category (::config/category config))
                        (= label (::config/label config)))))
         not-empty))

  (get-flat-configs-for-transactor-group [this transactor-group]
    (not-empty
     (cond
        ;; unassigned means "any configs not assigned to a transactor-group"
       (#{:unassigned ":unassigned"} transactor-group)
       (let [group-ids (eduction
                        (comp (filter ::config/transactor-group)
                              (map ::config/transactor-group)
                              (distinct))
                        (:transactor-groups data))
             assigned-configs (into #{}
                                    (mapcat #(p/get-flat-configs-for-transactor-group this %))
                                    group-ids)]
         (set/difference (:flat-configs data) assigned-configs))

        ;; Else lookup explicit transactor group assignments
       :else
       (let [group-assignments (select #(= transactor-group
                                           (::config/transactor-group %))
                                       (:transactor-groups data))]

         (into #{} cat
               (for [{::config/keys [tenant category label]} group-assignments]
                 (cond (and tenant category label) (p/get-flat-config this tenant category label nil)
                       (and tenant category) (p/get-flat-configs-for-tenant-and-category this tenant category nil)
                       (and category label) (p/get-flat-configs-for-category-and-label this category label nil)
                       (and tenant label) (->> (:flat-configs data)
                                               (select #(and (= tenant (::config/tenant %))
                                                             (= label (::config/label %)))))
                       category (->> (:flat-configs data)
                                     (select #(= category (::config/category %))))
                       label (->> (:flat-configs data)
                                  (select #(= label (::config/label %))))
                       tenant (->> (:flat-configs data)
                                   (select #(= tenant (::config/tenant %))))))))))))

(defn data->database-catalog [data]
  (DatabaseCatalog. data))

(defmulti load-catalog ::type)

(defmethod load-catalog :edn-file [{:keys [path]}]
  (let [data-map (load-catalog-data-from-file path)]
    (data->database-catalog data-map)))

(defmethod load-catalog :env-var [{:keys [env-var]}]
  (let [data-map (-> env-var required-env load-catalog-data-from-file)]
    (data->database-catalog data-map)))
