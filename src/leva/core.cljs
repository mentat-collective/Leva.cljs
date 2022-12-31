(ns leva.core
  (:require ["leva" :as l
             :refer
             [useControls useCreateStore useStoreContext Leva LevaPanel
              LevaStoreProvider]]
            ["react" :as react]
            [goog.object :as o]
            [reagent.core :as reagent]
            [reagent.ratom :as ratom]))


;; TODO for schema. IF we have an atom... then synchronize!
;; - if we have a schema... do NOT!
;; - if we have a schema, can we ALSO allow it to feed updates out? Can we do a schema AND then tie parts of it to an atom?

;; TODO scan for more goodies from storybook
;; https://leva.pmnd.rs/?path=/story/inputs-string--simple

;; TODO document specific options, like `:render` boolean fn,
;;
;; document other inputs https://github.com/pmndrs/leva/blob/main/docs/inputs.md
;;
;; folders? https://github.com/pmndrs/leva/blob/main/docs/getting-started.md#nested-folders


;; ## Numbers
;;
;; Increase / decrease numbers with arrow keys, with alt (±0.1) and shift (±10)
;; modifiers support.

;; ## Special Input Helpers

(defn button
  "Relevant opts: https://github.com/pmndrs/leva/blob/33b2d9948818c5828409e3cf65baed4c7492276a/packages/leva/src/types/public.ts#L47-L53"
  [on-click settings]
  (l/button on-click (clj->js settings)))

(defn button-group
  "Relevant type for opts: https://github.com/pmndrs/leva/blob/33b2d9948818c5828409e3cf65baed4c7492276a/packages/leva/src/types/public.ts#L55-L64"
  [opts]
  (l/buttonGroup (clj->js opts)))

(defn monitor
  "Relevant type for opts: https://github.com/pmndrs/leva/blob/33b2d9948818c5828409e3cf65baed4c7492276a/packages/leva/src/types/public.ts#L71-L77

  Tons of stuff with a monitor demo:
  https://codesandbox.io/s/github/pmndrs/leva/tree/main/demo/src/sandboxes/leva-busy

  The monitor is going to call a thunk for us that checks on something."
  [object-or-fn settings]
  (l/monitor object-or-fn (clj->js settings)))

(defn folder
  "Example: https://github.com/pmndrs/leva/blob/33b2d9948818c5828409e3cf65baed4c7492276a/packages/leva/stories/Folder.stories.tsx#L71

  Key is the folder name, value is the folder value...

  settings: https://github.com/pmndrs/leva/blob/33b2d9948818c5828409e3cf65baed4c7492276a/packages/leva/src/types/public.ts#L81-L87"
  [schema settings]
  (l/folder (clj->js schema)
            (clj->js settings)))

;; ## Configuration
;;
;; Customize the panel:
;; https://github.com/pmndrs/leva/blob/main/docs/configuration.md, see storybook
;; for more options

(defn ^:no-doc atom->schema-fn
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

(defn ^:no-doc opts->argv
  [{:keys [folder-name state options store folder-settings]}]
  (let [schema        (atom->schema-fn state options)
        hook-settings (when store #js {:store store})]
    (if folder-name
      [folder-name schema
       (when folder-settings (clj->js folder-settings))
       hook-settings]
      [schema hook-settings])))

;; ## Components

(defn GlobalConfig
  "Configures the global Leva store.

  https://github.com/pmndrs/leva/blob/main/packages/leva/src/components/Leva/Leva.tsx

  Takes all of these options except for \"store\":
  https://github.com/pmndrs/leva/blob/main/packages/leva/src/components/Leva/LevaRoot.tsx#L13

  PRovide children if you like for organization."
  [opts & children]
  (into [:<> [:> Leva opts]] children))

(defn SubPanel
  "Use this to create a subpanel. Children DO pick up on these settings.

  TODO document that we CAN actually use custom stores and contexts and pin a
  panel to a specific page element, once I figure out how to do that for
  jsxgraph and mathbox we'll be SOLID. Here is the demo of custom stores etc:
  https://codesandbox.io/s/github/pmndrs/leva/tree/main/demo/src/sandboxes/leva-advanced-panels?file=/src/App.jsx:0-26
  "
  [opts & children]
  (let [store (useCreateStore)]
    [:<>
     [:> LevaPanel (assoc opts :store store)]
     (into [:> LevaStoreProvider {:store store}] children)]))

(defn ^:no-doc Panel*
  "Function component that backs [[Panel]]."
  [opts]
  (when-not (:state opts)
    (throw
     (js/Error.
      (str "Error: we currently require a :state opt."))))

  (let [!state  (:state opts)
        ks      (keys (.-state !state))
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
                  (set
                   (clj->js
                    (select-keys @!state ks)))))]
           (fn unmount []
             (reagent/dispose! tracker)))
         js/undefined)))
    nil))

;; HUH! So the interface we want is either:
;;
;; - global store
;; - standalone store, anonymous
;; -
(defn Panel
  "We take `:state` and `:options`.

  Also

  `:folder-name`
  `:folder-settings` https://github.com/pmndrs/leva/blob/33b2d9948818c5828409e3cf65baed4c7492276a/packages/leva/src/types/public.ts#L81-L87

  `:store`
  `:hook-deps`

  TODO what good is hook deps? Why take that?"
  [opts]
  [:f> Panel* opts])


;; There are more demos that live here
;; https://github.com/pmndrs/leva/tree/main/demo/src/sandboxes, and we can
;; access them with the same URL.
;;
;; For plugins, here is an example:
;; https://github.com/pmndrs/leva/tree/main/packages/plugin-plot
;;
;; TODO maybe add links to the sandboxes in the notebook?
