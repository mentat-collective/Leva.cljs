(ns leva.sci
  "Functions and vars for installation of all namespaces into an SCI context."
  (:require [leva.core]
            [leva.types]
            [sci.core :as sci]
            [sci.ctx-store]))

(def leva-core-namespace
  (sci/copy-ns leva.core (sci/create-ns 'leva.core)))

(def leva-types-namespace
  (sci/copy-ns leva.types (sci/create-ns 'leva.types)))

(def ^{:doc "Map of symbol to SCI namespace object. This var is usable as the
`:namespaces` entry in an SCI context config."}
  namespaces
  {'leva.core leva-core-namespace
   'leva.types leva-types-namespace})

(def ^{:doc "SCI config that will install all of Leva.cljs into an SCI context,
  with no aliases registered."}
  config
  {:classes {'Math js/Math}
   :namespaces namespaces})

(defn install!
  "Called with no arguments, installs [[config]] into the shared SCI context
  store."
  []
  (sci.ctx-store/swap-ctx!
   sci/merge-opts
   config))
