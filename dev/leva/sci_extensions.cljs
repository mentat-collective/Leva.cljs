(ns leva.sci-extensions
  (:require ["@leva-ui/plugin-bezier" :as bezier]
            [leva.sci]
            [sci.ctx-store]
            [sci.core :as sci]))

;; ## SCI Customization

(leva.sci/install!)

(sci.ctx-store/swap-ctx!
 sci/merge-opts
 {:namespaces
  {'b {'bezier bezier/bezier}}})
