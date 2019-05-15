(defproject com.workiva.eva.catalog/client.alpha "2.0.1"
  :description "Client to the Eva Catalog Service"
  :plugins [[lein-modules "0.3.11"]
            [lein-cljfmt "0.6.4"]
            [lein-codox "0.10.3"]
            [lein-shell "0.5.0"]]

  :dependencies [[org.clojure/clojure "_"]
                 [clj-http-lite "_"]
                 [slingshot "_"]
                 [com.workiva.eva.catalog/common.alpha :version]]

  :deploy-repositories {"clojars"
                        {:url "https://repo.clojars.org"
                         :username :env/clojars_username
                         :password :env/clojars_password
                         :sign-releases false}}

  :aot [eva.catalog.client.alpha.client]
  :java-source-paths ["java-src"]
  :javac-options ["-encoding" "UTF-8"
                  "-source" "1.8"
                  "-target" "1.8"]

  :aliases {"docs" ["do" "clean-docs," "with-profile" "docs" "codox," "java-docs"]
            "clean-docs" ["shell" "rm" "-rf" "../documentation/client.alpha"]
            "java-docs" ["shell" "javadoc" "-d" "../documentation/client.alpha/java" "-notimestamp"
                         "./java-src/eva/catalog/client/alpha/HTTPCatalogClientImpl.java"
                         "./java-src/eva/catalog/client/alpha/HTTPCatalogClientImpl2.java"]}

  :codox {:metadata {:doc/format :markdown}
          :themes [:rdash]
          :html {:transforms [[:title]
                              [:substitute [:title "EVA Catalog - Alpha Client API Docs"]]
                              [:span.project-version]
                              [:substitute nil]
                              [:pre.deps]
                              [:substitute [:a {:href "https://clojars.org/com.workiva.eva.catalog/client.alpha"}
                                            [:img {:src "https://img.shields.io/clojars/v/com.workiva.eva.catalog/client.alpha.svg"}]]]]}
          :output-path "../documentation/client.alpha/clojure"}

  :profiles {:dev {:dependencies [[com.workiva.eva.catalog/server.alpha :version]
                                  [ring/ring-jetty-adapter "_"]]
                   :resource-paths ["test-data"
                                    "../common.alpha/test-data"]}
             :docs {:dependencies [[codox-theme-rdash "0.1.2"]]}})

