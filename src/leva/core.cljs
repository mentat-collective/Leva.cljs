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

;; TODO test that this CAN work if I want to test it out.
;; NOTE make a note that there is no guarantee this will work well.
#_["@leva-ui/plugin-plot" :as p]


;; ## Input Constructors

;; TODO kit replace these "settings" with a generic opts map and update in
;; defaults with settings.
;;
;; TODO test that z-order etc all work with these.

(defn button
  "Relevant opts: https://github.com/pmndrs/leva/blob/33b2d9948818c5828409e3cf65baed4c7492276a/packages/leva/src/types/public.ts#L47-L53"
  ([on-click]
   (button on-click {}))
  ([on-click settings]
   (let [defaults {:disabled false}]
     {:type (:button t/SpecialInputs)
      :onClick on-click
      :settings (merge defaults settings)})))

(defn button-group
  "Relevant type for opts: https://github.com/pmndrs/leva/blob/33b2d9948818c5828409e3cf65baed4c7492276a/packages/leva/src/types/public.ts#L55-L64"
  [opts]
  {:type (:button-group t/SpecialInputs)
   :opts opts})

(defn monitor
  "Relevant type for opts: https://github.com/pmndrs/leva/blob/33b2d9948818c5828409e3cf65baed4c7492276a/packages/leva/src/types/public.ts#L71-L77

  Tons of stuff with a monitor demo:
  https://codesandbox.io/s/github/pmndrs/leva/tree/main/demo/src/sandboxes/leva-busy

  The monitor is going to call a thunk for us that checks on something."
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
  "Function component that backs [[Panel]]."
  [opts]
  (let [[watch-id] (react/useState (str (random-uuid)))
        !state     (:atom opts)
        ks         (keys (.-state !state))
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
       (add-watch
        !state
        watch-id
        (fn [_ _ _ new-state]
          (set
           (clj->js
            (select-keys new-state ks)))))
       (fn unmount []
         (remove-watch !state watch-id))))
    nil))

(defn Controls
  "We take `:atom` and `:schema`.

  Also

  `:folder-name`
  `:folder-settings` https://github.com/pmndrs/leva/blob/33b2d9948818c5828409e3cf65baed4c7492276a/packages/leva/src/types/public.ts#L81-L87
  `:store`...

  TODO what good is hook deps? Why take that?"
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
