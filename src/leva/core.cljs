(ns leva.core
  "Reagent components and utilities exposing
  the [hooks](https://reactjs.org/docs/hooks-intro.html) declared by
  the [Leva](https://github.com/pmndrs/leva) components GUI.

  These components make it easy to synchronize state through Clojure's atom
  interface instead of
  [React hooks](https://reactjs.org/docs/hooks-intro.html) and callbacks."
  (:require ["leva" :as l]
            ["react" :as react]
            [leva.schema :as schema]
            [leva.types :as t]))

;; ## Special Input Constructors

(def ^:no-doc button-defaults
  {:disabled false})

(defn button
  "Returns a schema entry that defines a button, given a function `on-click` and a
  map `opt` of options."
  ([on-click]
   (button on-click {}))
  ([on-click opts]
   ;; TODO note what is happening here, trying to get a more sane settings deal.
   (let [settings (select-keys opts [:disabled])
         settings (merge button-defaults settings)]
     (-> {:type (:button t/SpecialInputs)
          :onClick on-click
          :settings settings}
         (merge (dissoc opts :type :onClick :disabled :settings))))))

(defn button-group
  "Relevant type for opts: https://github.com/pmndrs/leva/blob/33b2d9948818c5828409e3cf65baed4c7492276a/packages/leva/src/types/public.ts#L55-L64"
  ([opts]
   {:type (:button-group t/SpecialInputs)
    :opts opts})
  ([label opts]
   {:type (:button-group t/SpecialInputs)
    :opts {:label label
           :opts opts}}))

(defn monitor
  "Relevant type for opts: https://github.com/pmndrs/leva/blob/33b2d9948818c5828409e3cf65baed4c7492276a/packages/leva/src/types/public.ts#L71-L77

  Tons of stuff with a monitor demo:
  https://codesandbox.io/s/github/pmndrs/leva/tree/main/demo/src/sandboxes/leva-busy

  The monitor is going to call a thunk for us that checks on something.

  settings can be graph or interval."
  ([object-or-fn]
   (monitor object-or-fn {}))
  ([object-or-fn settings]
   {:type (:monitor t/SpecialInputs)
    :objectOrFn object-or-fn
    :settings settings}))

(defn folder
  "Example: https://github.com/pmndrs/leva/blob/33b2d9948818c5828409e3cf65baed4c7492276a/packages/leva/stories/Folder.stories.tsx#L71

  Key is the folder name, value is this

  settings: https://github.com/pmndrs/leva/blob/33b2d9948818c5828409e3cf65baed4c7492276a/packages/leva/src/types/public.ts#L81-L87"
  ([schema]
   (folder schema {}))
  ([schema settings]
   {:type t/FolderType
    :schema schema
    :settings settings}))

;; ## Components

(defn GlobalConfig
  "Configures the global Leva store.

  https://github.com/pmndrs/leva/blob/main/packages/leva/src/components/Leva/Leva.tsx

  Takes all of these options except for \"store\":
  https://github.com/pmndrs/leva/blob/main/packages/leva/src/components/Leva/LevaRoot.tsx#L13

  PRovide children if you like for organization."
  [opts & children]
  (into [:<> [:> l/Leva opts]] children))

(defn SubPanel
  "Use this to create a subpanel. Children DO pick up on these settings."
  [opts & children]
  (let [store (l/useCreateStore)]
    [:<>
     [:> l/LevaPanel (assoc opts :store store)]
     (into [:> l/LevaStoreProvider {:store store}] children)]))

(defn ^:no-doc Controls*
  "Function component that backs [[Panel]]. atom is optional now!"
  [opts]
  (let [[watch-id] (react/useState (str (random-uuid)))
        !state     (:atom opts)
        initial    (if !state (.-state !state) {})
        ks         (keys initial)
        opts       (update opts :store #(or % (l/useStoreContext)))

        ;; NOTE that if we want to add a hook deps array here, we can conj it
        ;; onto the end of the vector returned by `opts->argv`. In the current
        ;; implementation, this hook is called on each re-render.
        ;;
        ;; NOTE if we don't apply the function wrapper above, the return value
        ;; here is no longer a pair.
        [_ set] (apply l/useControls (schema/opts->argv opts))]
    (react/useEffect
     (fn mount []
       (if !state
         (do (add-watch
              !state
              watch-id
              (fn [_ _ _ new-state]
                (set
                 (clj->js
                  (select-keys new-state ks)))))
             (fn unmount []
               (remove-watch !state watch-id)))
         js/undefined)))
    nil))

(defn Controls
  "We take `:atom` and `:schema`.

  Also

  `:folder {:name :settings}` https://github.com/pmndrs/leva/blob/33b2d9948818c5828409e3cf65baed4c7492276a/packages/leva/src/types/public.ts#L81-L87
  `:store`..."
  [opts]
  [:f> Controls* opts])

;; There are more demos that live here
;; https://github.com/pmndrs/leva/tree/main/demo/src/sandboxes, and we can
;; access them with the same URL.
;;
;; For plugins, here is an example:
;; https://github.com/pmndrs/leva/tree/main/packages/plugin-plot
;;
;; TODO maybe add links to the sandboxes in the notebook?
