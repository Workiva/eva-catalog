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

(ns eva.catalog.server.alpha.handler
  (:require [compojure.core :as cc]
            [compojure.route :as route]
            [ring.util.response :as r]
            [ring.middleware.format :as rmf]
            [eva.catalog.common.alpha.protocols :as p]
            [ring.middleware.defaults :as d :refer [wrap-defaults site-defaults]]))

(defn build-routes [catalog]
  (-> (cc/defroutes app-routes
        (cc/GET "/status" [] (r/response "All systems nominal."))

        (cc/GET "/flat-peer-config" [tenant category label]
          (if-let [config (p/get-flat-config catalog tenant category label :peer)]
            {:body config}
            {:status 404
             :body (format "could not find a config for tenant %s, category %s, and label %s"
                           tenant category label)}))

        (cc/GET "/flat-peer-configs-for-tenant-and-category" [tenant category]
          (if-let [configs (p/get-flat-configs-for-tenant-and-category catalog tenant category :peer)]
            {:body configs}
            {:status 404
             :body (format "could not find configs for tenant %s and category %s"
                           tenant category)}))

        (cc/GET "/flat-peer-configs-for-category-and-label" [category label]
          (if-let [configs (p/get-flat-configs-for-category-and-label catalog category label :peer)]
            {:body configs}
            {:status 404
             :body (format "could not find configs for category %s and label %s"
                           category label)}))
        (cc/GET "/flat-transactor-group-configs" [transactor-group]
          (if-some [configs (p/get-flat-configs-for-transactor-group catalog transactor-group)]
            {:body configs}
            {:status 404
             :body (format "could not find configs for transactor-group: %s" transactor-group)}))

        (route/not-found "Not Found"))
      (rmf/wrap-restful-format)
      (wrap-defaults site-defaults)))
