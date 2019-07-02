(defproject com.workiva.eva.catalog/common.alpha "2.0.3"
  :plugins [[lein-modules "0.3.11"]
            [lein-cljfmt "0.6.4"]
            [lein-codox "0.10.3"]
            [lein-shell "0.5.0"]]

  :dependencies [[org.clojure/clojure "_"]]

  :deploy-repositories {"clojars"
                        {:url "https://repo.clojars.org"
                         :username :env/clojars_username
                         :password :env/clojars_password
                         :sign-releases false}}

  :aliases {"docs" ["do" "clean-docs," "with-profile" "docs" "codox"]
            "clean-docs" ["shell" "rm" "-rf" "../documentation/common.alpha"]}

  :codox {:metadata {:doc/format :markdown}
          :themes [:rdash]
          :html {:transforms [[:title]
                              [:substitute [:title "EVA Catalog - Common Lib Alpha API Docs"]]
                              [:span.project-version]
                              [:substitute nil]
                              [:pre.deps]
                              [:substitute [:a {:href "https://clojars.org/com.workiva.eva.catalog/common.alpha"}
                                            [:img {:src "https://img.shields.io/clojars/v/com.workiva.eva.catalog/common.alpha.svg"}]]]]}
          :output-path "../documentation/common.alpha/clojure"}

  :profiles {:dev {:resource-paths ["test-data"]}
             :docs {:dependencies [[codox-theme-rdash "0.1.2"]]}})

