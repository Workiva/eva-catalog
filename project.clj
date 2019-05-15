(defproject com.workiva.eva/catalog-parent "2.0.0"
  :plugins [[lein-modules "0.3.11"]
            [lein-cljfmt "0.6.4"]]

  :modules {:versions {org.clojure/clojure "1.9.0"
                       clj-http-lite "0.3.0"
                       slingshot "0.12.2"
                       compojure "1.5.1"
                       ring-middleware-format "0.7.2"
                       ring/ring-core "1.6.3"
                       ring/ring-defaults "0.3.1"
                       ring/ring-jetty-adapter "1.6.3"
                       ring/ring-mock "0.3.0"}})
