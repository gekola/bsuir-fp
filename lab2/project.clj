(defproject lab2 "0.1.0-SNAPSHOT"
  :description "Multithreaded recursive crowler bot."
  :dependencies [[org.clojure/clojure "1.6.0"],
                 [http-kit "2.1.16"]]
  :main ^:skip-aot lab2.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
