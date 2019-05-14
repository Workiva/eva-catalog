(defproject workivabuild/prefetch-deps "0.0.0"
  :modules {:parent "../.."}

  ;; Dependencies and plugins listed here will be pre-fetched
  ;; and cached as part of the workiva build process
  :plugins [[lein-modules "0.3.11"]
            [lein-cljfmt "0.6.4"]
            [lein-codox "0.10.3"]
            [lein-shell "0.5.0"]]

  :dependencies [[org.clojure/clojure "_"]
                 [clj-http-lite "_"]
                 [slingshot "_"]
                 [compojure "_"]
                 [ring-middleware-format "_"]
                 [ring/ring-core "_"]
                 [ring/ring-defaults "_"]
                 [ring/ring-jetty-adapter "_"]
                 [ring/ring-mock "_"]])
