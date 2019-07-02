(defproject com.workiva.eva.catalog/server.alpha "2.0.3"
  :plugins [[lein-modules "0.3.11"]
            [lein-ring "0.9.7"]
            [lein-cljfmt "0.6.4"]
            [lein-codox "0.10.3"]
            [lein-shell "0.5.0"]]

  :dependencies [[org.clojure/clojure "_"]
                 [compojure "_"]
                 [ring-middleware-format "_"]
                 [ring/ring-core "_"]
                 [ring/ring-defaults "_"]
                 [ring/ring-jetty-adapter "_"]
                 [com.workiva.eva.catalog/common.alpha :version]]

  :deploy-repositories {"clojars"
                        {:url "https://repo.clojars.org"
                         :username :env/clojars_username
                         :password :env/clojars_password
                         :sign-releases false}}

  :aliases {"docs" ["do" "clean-docs," "with-profile" "docs" "codox"]
            "clean-docs" ["shell" "rm" "-rf" "../documentation/server.alpha"]}

  :aot [eva.catalog.server.alpha.server]
  :ring {:handler eva.catalog.server.alpha.handler/app}
  :main eva.catalog.server.alpha.server
  :uberjar-name "eva-catalog-server.jar"

  :codox {:metadata {:doc/format :markdown}
          :themes [:rdash]
          :html {:transforms [[:title]
                              [:substitute [:title "EVA Catalog - Alpha Server API Docs"]]
                              [:span.project-version]
                              [:substitute nil]
                              [:pre.deps]
                              [:substitute [:a {:href "https://clojars.org/com.workiva.eva.catalog/server.alpha"}
                                            [:img {:src "https://img.shields.io/clojars/v/com.workiva.eva.catalog/server.alpha.svg"}]]]]}
          :output-path "../documentation/server.alpha/clojure"}

  :profiles {:dev {:dependencies [[ring/ring-mock "_"]]
                   :resource-paths ["../common.alpha/test-data"]}
             :debug-compile {:javac-options ["-g"]}
             :docs {:dependencies [[codox-theme-rdash "0.1.2"]]}})
