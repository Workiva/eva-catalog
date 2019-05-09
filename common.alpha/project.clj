(defproject com.workiva.eva.catalog/common.alpha "0.1.12"
  :plugins [[lein-modules "0.3.11"]
            [lein-cljfmt "0.6.4"]]
  :dependencies [[org.clojure/clojure "_"]]

  :profiles {:dev {:resource-paths ["test-data"]}})

