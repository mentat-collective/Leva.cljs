(ns leva.core
  (:require ["leva" :refer [useControls Leva]]
            ["react" :as react]
            [goog.object :as o]
            [reagent.core :as reagent]))

;; ## SCI Customization

;; TODO: can I tie the useControls to a specific panel instance that I create? I
;; asked in the channel.
;;
;; TODO if it's not a reagent atom, don't install the tracker.

;; TODO take a `:state` key vs top level
;;
;; TODO scan for more goodies from storybook
;; https://leva.pmnd.rs/?path=/story/inputs-string--simple

;; TODO take OPTIONS for the kv pairs
;; TODO document specific options, like `:render` boolean fn,
;;
;; document other inputs https://github.com/pmndrs/leva/blob/main/docs/inputs.md
;;
;; folders? https://github.com/pmndrs/leva/blob/main/docs/getting-started.md#nested-folders


;; ## Numbers
;;
;; Increase / decrease numbers with arrow keys, with alt (±0.1) and shift (±10)
;; modifiers support.

;; ## Configuration
;;
;; Customize the panel:
;; https://github.com/pmndrs/leva/blob/main/docs/configuration.md, see storybook
;; for more options


(defn Panel* [opts]
  (when-not (:state opts)
    (throw
     (js/Error.
      (str "Error: we currently require a :state opt."))))

  (let [!state (:state opts)
        [_ set] (useControls
                 (fn []
                   (reduce-kv
                    (fn [acc k v]
                      (doto acc
                        (o/set
                         (name k)
                         #js {"value" v
                              "onChange"
                              (fn [value _ _]
                                (swap! !state assoc k value))})))
                    (js-obj)
                    @!state)))]
    (react/useEffect
     (fn mount []
       (let [tracker
             (reagent/track!
              (fn []
                (set (clj->js @!state))))]
         (fn unmount []
           (reagent/dispose! tracker)))))
    nil))

(defn PanelOptions [opts]
  [:> Leva opts])

(defn Panel [opts]
  [:f> Panel* opts])
