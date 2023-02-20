(ns user
  (:require [mentat.clerk-utils.build :as b]))

(def index
  "dev/leva/notebook.clj")

(def defaults
  {:index index
   :browse? true
   :watch-paths ["dev"]
   :cljs-namespaces '[leva.sci-extensions]})

(def static-defaults
  (assoc defaults
         :browse? false
         :cname "leva.mentat.org"
         :git/url "https://github.com/mentat-collective/leva.cljs"))

(defn serve!
  ([] (serve! {}))
  ([opts]
   (b/serve!
    (merge defaults opts))))

(def halt! b/halt!)

(defn build! [opts]
  (b/build!
   (merge static-defaults opts)))
