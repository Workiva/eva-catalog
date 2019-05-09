(defproject com.workiva.eva.catalog/server.alpha "0.1.12"
  :plugins [[lein-modules "0.3.11"]
            [lein-ring "0.9.7"]
            [lein-cljfmt "0.6.4"]]
  :dependencies [[org.clojure/clojure "_"]
                 [compojure "_"]
                 [ring-middleware-format "_"]
                 [ring/ring-core "_"]
                 [ring/ring-defaults "_"]
                 [ring/ring-jetty-adapter "_"]
                 [com.workiva.eva.catalog/common.alpha :version]]

  :aot [eva.catalog.server.alpha.server]
  :ring {:handler eva.catalog.server.alpha.handler/app}
  :main eva.catalog.server.alpha.server
  :uberjar-name "eva-catalog-server.jar"

  :profiles {:dev {:dependencies [[ring/ring-mock "_"]]
                   :resource-paths ["../common.alpha/test-data"]}
             :debug-compile {:javac-options ["-g"]}})

