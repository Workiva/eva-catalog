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

(ns eva.catalog.server.alpha.server
  (:require [ring.adapter.jetty :as raj]
            [eva.catalog.common.alpha.utils :refer [env]]
            [eva.catalog.common.alpha.catalog :as c]
            [eva.catalog.server.alpha.handler :refer [build-routes]])
  (:gen-class))

(defn -main [& args]
  (let [catalog (c/load-catalog {::c/type :env-var :env-var "EVA_CATALOG_DATA"})
        host (env "EVA_CATALOG_HOST" "0.0.0.0")
        port (Integer/parseInt (env "EVA_CATALOG_PORT" "3000"))
        routes (build-routes catalog)]
    (raj/run-jetty routes {:port port :host host})))
