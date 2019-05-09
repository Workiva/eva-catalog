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

(ns eva.catalog.common.alpha.utils
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.set :as s])
  (:import [java.io PushbackReader]))

(defn load-edn-data-from-file [path]
  (with-open [r (PushbackReader. (io/reader path))]
    (edn/read r)))

(def ^:dynamic *env-overrides* nil)

(defn env
  ([name]
   (if (contains? *env-overrides* name)
     (get *env-overrides* name)
     (System/getenv name)))

  ([name default]
   (if (contains? *env-overrides* name)
     (get *env-overrides* name)
     (or (System/getenv name)
         default))))

(defn required-env [name]
  (if (contains? *env-overrides* name)
    (get *env-overrides* name)
    (or (System/getenv name)
        (throw (IllegalStateException. (str "ENV variable not set: " name))))))

(defn join
  "When passed 2 rels, returns the rel corresponding to the natural
  join on the provided key map.

  Modified from clojure.set. Removes the size-based rel-flipping
  optimization in order to provide consistent merging behavior on shared
  but non-joined keys: will always merge rows from yrel onto xrel."
  ([xrel yrel km]
   (let [idx (s/index xrel (vals km))]
     (reduce (fn [ret x]
               (let [found (idx (s/rename-keys (select-keys x (keys km)) km))]
                 (if found
                   (reduce #(conj %1 (merge %2 x)) ret found)
                   ret)))
             #{} yrel))))
