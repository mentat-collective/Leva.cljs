(ns user
  (:require [babashka.fs :as fs]
            [clojure.java.shell :refer [sh]]
            [clojure.string]
            [nextjournal.clerk :as clerk]
            [nextjournal.clerk.config :as config]
            [nextjournal.clerk.view]
            [nextjournal.clerk.viewer :as cv]
            [shadow.cljs.devtools.api :as shadow]))

(def index
  "dev/leva/notebook.clj")

(def build-target
  {:index index
   :git/url "https://github.com/mentat-collective/leva.cljs"})

(def ^{:doc "static site defaults for local and github-pages modes."}
  defaults
  {:out-path   "public"
   :cas-prefix "/"})

(def ^{:doc "static site defaults for Clerk's Garden CDN."}
  garden-defaults
  {:cas-prefix "/mentat-collective/leva.cljs/commit/$GIT_SHA/"})

(defn start!
  "Runs [[clerk/serve!]] with our custom JS. Run this after generating custom JS
  with shadow in either production or `watch` mode."
  []
  (swap! config/!resource->url
         assoc
         "/js/viewer.js" "http://localhost:8765/js/main.js")
  (clerk/serve!
   {:browse? true
    :watch-paths ["dev"]})
  (Thread/sleep 500)
  (clerk/show! index))

(defn git-sha
  "Returns the sha hash of this project's current git revision."
  []
  (-> (sh "git" "rev-parse" "HEAD")
      (:out)
      (clojure.string/trim)))

(defn replace-sha-template!
  "Given some `path`, modifies the file at `path` replaces any occurence of the
  string `$GIT_SHA` with the actual current sha of the repo."
  ([path]
   (replace-sha-template! path (git-sha)))
  ([path sha]
   (-> (slurp path)
       (clojure.string/replace "$GIT_SHA" sha)
       (->> (spit path)))))

(defn static-build!
  "This task is used to generate static sites for local use, github pages
  deployment and Clerk's Garden CDN.

  Accepts a map of options `opts` and runs the following tasks:

  - Slurps the output of the shadow-cljs `:clerk` build from `public/js/main.js`
    and pushes it into a CAS located at `(str (:out-path opts) \"/js/_data\")`.

  - Configures Clerk to generate files that load the js from the generated name
    above, stored in `(str (:cas-prefix opts) \"/js/_data\")`

  - Creates a static build of the single namespace [[index]] at `(str (:out-path
    opts) \"/index.html\")`

  - Replaces all instances of the string $GIT_SHA in `index.html` with the
    actual sha of the library.

  All `opts` are forwarded to [[nextjournal.clerk/build!]]."
  [opts]
  (let [{:keys [out-path cas-prefix]} (merge defaults opts)
        sha (or (:git/sha opts) (git-sha))
        cas (cv/store+get-cas-url!
             {:out-path (str out-path "/js") :ext "js"}
             (fs/read-all-bytes "public/js/main.js"))]
    (swap! config/!resource->url assoc
           "/js/viewer.js"
           (str cas-prefix "js/" cas))
    (clerk/build!
     (merge build-target
            (assoc opts :out-path out-path
                   :git/sha sha)))
    (replace-sha-template!
     (str out-path "/index.html"))))

(defn garden!
  "Standalone executable function that runs [[static-build!]] configured for
  Clerk's Garden. See [[garden-defaults]] for customizations
  to [[static-build!]]."
  [opts]
  (println "Running npm install...")
  (println
   (sh "npm" "install"))
  (shadow/release! :clerk)
  (static-build!
   (merge garden-defaults opts)))
