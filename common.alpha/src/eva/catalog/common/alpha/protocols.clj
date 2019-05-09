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

(ns eva.catalog.common.alpha.protocols)

(defprotocol DatabaseCatalogRead
  (get-flat-config [catalog tenant category label role]
    "Returns a full configuration map for the provided service-name and
     database name from the catalog. ")
  (get-flat-configs-for-tenant-and-category [catalog tenant category role]
    "Returns a set of configuration maps for the given tenant and category")
  (get-flat-configs-for-category-and-label [catalog category label role]
    "Returns a set of configuration maps for the given category and label")
  (get-flat-configs-for-transactor-group [catalog transactor-group]
    "Returns oa set of configuration maps for the given transactor group"))
