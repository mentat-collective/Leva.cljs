(ns leva.core
  (:require ["leva" :refer [useControls Leva]]
            ["react" :as react]
            [goog.object :as o]
            [reagent.core :as reagent]
            [reagent.ratom :as ratom]))

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

  (let [!state  (:state opts)
        options (:options opts)
        [_ set] (useControls
                 (fn []
                   (reduce-kv
                    (fn [acc k v]
                      (let [on-change
                            (fn [value _ _]
                              (when (not= value (get (.-state !state) k ::not-found))
                                (swap! !state assoc k value)))]
                        (doto acc
                          (o/set
                           (name k)
                           ;; TODO Note that `k-opts` must be a map.
                           (if-let [k-opts (get options k nil)]
                             (clj->js
                              (assoc k-opts :value v :onChange on-change))
                             #js {"value" v "onChange" on-change})))))
                    (js-obj)
                    @!state)))]
    (react/useEffect
     (fn mount []
       (if (satisfies? ratom/IReactiveAtom !state)
         (let [tracker
               (reagent/track!
                (fn []
                  (set (clj->js @!state))))]
           (fn unmount []
             (reagent/dispose! tracker)))
         js/undefined)))
    nil))

(defn PanelOptions [opts]
  [:> Leva opts])

(defn Panel [opts]
  [:f> Panel* opts])

;; TODO document that we CAN actually use custom stores and contexts and pin a
;; panel to a specific page element, once I figure out how to do that for
;; jsxgraph and mathbox we'll be SOLID. Here is the demo of custom stores etc:
;; https://codesandbox.io/s/github/pmndrs/leva/tree/main/demo/src/sandboxes/leva-advanced-panels?file=/src/App.jsx:0-26
;;
;; There are more demos that live here
;; https://github.com/pmndrs/leva/tree/main/demo/src/sandboxes, and we can
;; access them with the same URL.
;;
;; For plugins, here is an example:
;; https://github.com/pmndrs/leva/tree/main/packages/plugin-plot
