(defproject goldfish "0.1.0-SNAPSHOT"
  :description "Attempt to write online game in Clojure(Script)"
  :url "http://example.com/FIXME"
  :min-lein-version "2.5.3"

  :dependencies
  [[com.taoensso/sente        "1.15.0"]
   [com.taoensso/timbre       "4.10.0"]
   [compojure                 "1.6.1"]
   [environ                   "1.1.0"]
   [hiccup                    "1.0.5"]
   [http-kit                  "2.4.0-alpha6"]
   [nano-id                   "0.10.0"]
   [org.clojure/clojure       "1.10.0"]
   [org.clojure/clojurescript "1.10.597"]
   [org.clojure/core.async    "1.0.567"]
   [org.clojure/test.check    "1.0.0"]
   [org.clojure/tools.nrepl   "0.2.13"]
   [reagent                   "0.8.1"]
   [ring                      "1.8.0"]
   [ring-cors                 "0.1.13"]
   [ring/ring-defaults        "0.3.2"]]

  :plugins
  [[cider/cider-nrepl "0.22.4"]
   [lein-figwheel     "0.5.19"]
   [lein-cljsbuild    "1.1.7"]
   [lein-ring         "0.12.5"]
   [lein-environ      "1.1.0"]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :main goldfish.server.main

  :ring {:handler goldfish.handler/app}

  :profiles
  {:dev
   {:env {:http-port 5000 :dev? "true"}
    :cljsbuild {:builds [{:id :goldfish-client
                          :source-paths ["src"]
                          :figwheel {}
                          :compiler {:main goldfish.client.main
                                     :asset-path "js/compiled/out"
                                     :output-to "resources/public/js/compiled/goldfish.js"
                                     :output-dir "resources/public/js/compiled/out"
                                     :source-map-timestamp true}}]}
    ;; :dependencies
    ;; [[javax.servlet/servlet-api "2.5"]
    ;;  [ring/ring-mock            "0.3.2"]]
    }}

  :figwheel {:css-dirs ["resources/public/css"] })
