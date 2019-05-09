(defproject com.workiva.eva.catalog/client.alpha "0.1.12"
  :description "Client to the Eva Catalog Service"
  :plugins [[lein-modules "0.3.11"]
            [lein-cljfmt "0.6.4"]]
  :dependencies [[org.clojure/clojure "_"]
                 [clj-http-lite "_"]
                 [slingshot "_"]
                 [com.workiva.eva.catalog/common.alpha :version]]

  :aot [eva.catalog.client.alpha.client]
  :java-source-paths ["java-src"]
  :javac-options ["-encoding" "UTF-8"
                  "-source" "1.8"
                  "-target" "1.8"]

  :profiles {:dev {:dependencies [[com.workiva.eva.catalog/server.alpha :version]
                                  [ring/ring-jetty-adapter "_"]]
                   :resource-paths ["test-data"
                                    "../common.alpha/test-data"]}})

