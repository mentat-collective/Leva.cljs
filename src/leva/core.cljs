(ns leva.core
  (:require ["leva" :refer [useControls useCreateStore useStoreContext Leva LevaPanel
                            LevaStoreProvider]]
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

(defn atom->schema-fn
  "I guess this is good if we have an atom and options. We also could just have a
  schema... but then how are they going to read the state back out? You need
  SOME way to deal with the handlers. I guess we can say, look, if you are using
  this library, you are going to communicate via an atom."
  [!state options]
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
     @!state)))

(defn opts->argv
  [{:keys [folder-name state options store folder-settings]}]
  (let [schema        (atom->schema-fn state options)
        hook-settings (when store #js {:store store})]
    (if folder-name
      [folder-name schema
       (when folder-settings (clj->js folder-settings))
       hook-settings]
      [schema hook-settings])))

(defn Panel*
  "We take `:state` and `:options`.

  Also

  `:folder-name`
  `:folder-settings` https://github.com/pmndrs/leva/blob/33b2d9948818c5828409e3cf65baed4c7492276a/packages/leva/src/types/public.ts#L81-L87

  `:store`
  `:hook-deps`

  TODO what good is hook deps? Why take that?"
  [opts]
  (when-not (:state opts)
    (throw
     (js/Error.
      (str "Error: we currently require a :state opt."))))

  (let [!state  (:state opts)
        opts    (update opts :store #(or % (useStoreContext)))
        ;; NOTE that if we want to add a hook deps array here, we can conj it
        ;; onto the end of the vector returned by `opts->argv`. In the current
        ;; implementation, this hook is called on each re-render.
        [_ set] (apply useControls (opts->argv opts))]
    (react/useEffect
     (fn mount []
       ;; NOTE in docs that we only install if it's reactive.
       (if (satisfies? ratom/IReactiveAtom !state)
         (let [tracker
               (reagent/track!
                (fn mount []
                  (set (clj->js @!state))))]
           (fn unmount []
             (reagent/dispose! tracker)))
         js/undefined)))
    nil))

;; HUH! So the interface we want is either:
;;
;; - global store
;; - standalone store, anonymous
;; -
(defn Panel [opts]
  [:f> Panel* opts])

(defn GlobalConfig
  "Configures the global Leva store.

  https://github.com/pmndrs/leva/blob/main/packages/leva/src/components/Leva/Leva.tsx

  Takes all of these options except for \"store\":
  https://github.com/pmndrs/leva/blob/main/packages/leva/src/components/Leva/LevaRoot.tsx#L13"
  [opts & children]
  (into [:<> [:> Leva opts]] children))

(defn SubPanel
  "Use this to create a subpanel. Children DO pick up on these settings."
  [opts & children]
  (let [store (useCreateStore)]
    [:<>
     [:> LevaPanel (assoc opts :store store)]
     (into [:> LevaStoreProvider {:store store}] children)]))

;; ## DEMO

(defn Cake
  "Example for testing stores"
  [!state]
  (let [store (useCreateStore)]
    [:div {:style {:display "grid"
                   :width 300
                   :gridRowGap 10
                   :padding 10
                   :background "#fff"}}
     [:> LevaPanel {:store store
                    :fill true
                    :drag false}]
     [:> LevaStoreProvider {:store store}
      [Panel {:state !state}]]]))

;; <div
;; style={{
;;         display: 'grid',
;;         width: 300,
;;         gridRowGap: 10,
;;         padding: 10,
;;         background: '#fff',
;;         }}>
;; <LevaPanel store={store1} fill flat titleBar={false} />
;; <LevaPanel store={store2} fill flat titleBar={false} />
;; <LevaStoreProvider store={store1}>
;; <MyComponent />
;; </LevaStoreProvider>
;; </div>


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
