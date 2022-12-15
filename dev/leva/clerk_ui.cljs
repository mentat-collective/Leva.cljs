(ns leva.clerk-ui
  (:require [leva.core]
            [nextjournal.clerk.sci-env]
            [sci.ctx-store]
            [sci.core :as sci]))

;; ## SCI Customization

(sci.ctx-store/swap-ctx!
 sci/merge-opts
 {:classes    {'Math js/Math}
  :aliases    {'leva 'leva.core}
  :namespaces
  {'leva.core
   (sci/copy-ns leva.core (sci/create-ns 'leva.core))}})
