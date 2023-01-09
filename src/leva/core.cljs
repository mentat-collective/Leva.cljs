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
  map `opt` of options.

  The `opts` allowed are any found in the types

  - [`ButtonSettings`](https://github.com/pmndrs/leva/blob/33b2d9948818c5828409e3cf65baed4c7492276a/packages/leva/src/types/public.ts#L47)
  - [`GenericSchemaItemOptions`](https://github.com/pmndrs/leva/blob/33b2d9948818c5828409e3cf65baed4c7492276a/packages/leva/src/types/public.ts#L144-L149)

  In [public.ts](https://github.com/pmndrs/leva/blob/33b2d9948818c5828409e3cf65baed4c7492276a/packages/leva/src/types/public.ts)
  in [Leva's repository](https://github.com/pmndrs/leva). "
  ([on-click]
   (button on-click {}))
  ([on-click opts]
   (let [settings (select-keys opts [:disabled])
         settings (merge button-defaults settings)]
     (-> {:type (:button t/SpecialInputs)
          :onClick on-click
          :settings settings}
         (merge (dissoc opts :type :onClick :disabled :settings))))))

(defn button-group
  "Returns a schema entry that defines a button group, given either

  - a label and a map of `{<string> (fn [get] ,,,)}`
  - only the map

  Where `get` is of type `string => value`, and allows you to query the internal
  leva store.

  Feel free to ignore `get` and query the stateful atom associated with
  this [[Controls]] instance from the value function."
  ([opts]
   {:type (:button-group t/SpecialInputs)
    :opts opts})
  ([label opts]
   {:type (:button-group t/SpecialInputs)
    :opts {:label label
           :opts opts}}))

(defn monitor
  "Returns a schema entry that defines a \"monitor\", given as a first argument
  either

  - a no-arg function that returns a number, or
  - a react `MutableRefObject` returned by `useRef`, where `(.-current ref)`
    returns a number

  and an optional settings map as a second argument. The supported (optional)
  settings are

  - `:graph`: if true, the returned monitor shows a graph. if false, the monitor
    displays a number.

  - `:interval`: the number of milliseconds to wait between queries of
    `object-or-fn`."
  ([object-or-fn]
   (monitor object-or-fn {}))
  ([object-or-fn settings]
   {:type (:monitor t/SpecialInputs)
    :objectOrFn object-or-fn
    :settings settings}))

(defn folder
  "Given a sub-schema `schema` and an optional map of folder `settings`, returns a
  schema entry that wraps `schema` in a subfolder.

  The supported (optional) settings are

  - `:collapsed` if true, the folder will be collapsed on initial render.
    Defaults to false.

  - `:render` (fn [get] <boolean>), providing dynamic control or whether or not
    the folder appears.

      `get` is of type `string => value`, and allows you to query the internal
      leva store. If the `:render` fn returns true, this folder will be rendered in
      the panel; if false it won't render.

  - `:color` color string, sets the color of the folder title.

  - `:order` number, sets the order of this folder relative to other components
    at the same level."
  ([schema]
   (folder schema {}))
  ([schema settings]
   {:type t/FolderType
    :schema schema
    :settings settings}))

;; ## Components
;;
;; This section defines the top-level components available for customizing and
;; declaring elements of the global panel and any subpanels you might create.

(defn Config
  "Component that configures a Leva panel with the supplied map of `opts` without
  explicitly rendering any inputs into it. If `:store` is not provided,
  configures the globally available Leva panel.

  See the
  type [`LevaRootProps`](https://github.com/pmndrs/leva/blob/main/packages/leva/src/components/Leva/LevaRoot.tsx#L13-L93)
  for a full list of available entries for `opts` and documentation for each.

  You can pass any number of `children` components if you like for
  organizational purposes.

  If you pass `:store`, any [[Controls]] component in `children` will use that
  store vs the store of the global panel.

  NOTE: We recommend using [[SubPanel]] to declare non-global Leva panels,
  rather than worrying about creating and passing your own Leva store via
  `:store`. But for advanced use cases, please feel free!"
  [opts & children]
  (if-let [store (:store opts)]
    [:<>
     [:> l/LevaPanel (assoc opts :store store)]
     (into [:> l/LevaStoreProvider {:store store}] children)]
    (into [:<> [:> l/Leva opts]] children)))

(defn SubPanel
  "Component that configures a non-global, standalone Leva panel with the supplied
  map of `opts`.p

  Any instance of [[Controls]] passed as `children` will render into this
  subpanel and not touch the global store.

  See the
  type [`LevaRootProps`](https://github.com/pmndrs/leva/blob/main/packages/leva/src/components/Leva/LevaRoot.tsx#L13-L93)
  for a full list of available entries for `opts` and documentation for each."
  [opts & children]
  {:pre [(not (:store opts))]}
  (assert
   (not (:store opts))
   (str "`:store` is not supported by [[leva.core/SubPanel]]. "
        "If you'd like to provide your own :store, "
        "see [[leva.core/Config]]."))
  (let [store (l/useCreateStore)]
    (into [Config (assoc opts :store store)]
          children)))

(defn ^:no-doc Controls*
  "Function component that backs [[Controls]]. See [[Controls]] for detailed
  documentation."
  [opts]
  (when-not (or (:atom opts) (:schema opts))
    (throw
     (js/Error.
      (str "Error: we currently require either "
           "an `:atom` or `:schema` option (or both!)"))))

  (let [[watch-id] (react/useState (str (random-uuid)))
        !state     (:atom opts)
        initial    (if !state (.-state !state) {})
        ks         (keys initial)
        opts       (update opts :store #(or % (l/useStoreContext)))

        ;; NOTE that if we want to add a hook deps array here, we can conj it
        ;; onto the end of the vector returned by [[leva.schema/opts->argv]]. In
        ;; the current implementation, this hook is called on each re-render.
        ;;
        ;; I think we'll need to do this if we want to auto-refresh when schema
        ;; changes occur.
        ;;
        ;; NOTE if we don't apply the function wrapper
        ;; in [[leva.schema/opts->argv]], the return value here is no longer a
        ;; pair.
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
  "Component that renders inputs into a global or local Leva control panel,
  possibly synchronizing the panel's state into a provided atom.

  Placing this component anywhere in the render tree will add controls to the
  global Leva panel.

  To modify a local Leva panel, nest this component inside of a [[SubPanel]].

  Supported `opts` are:

  - `:schema`: A leva schema definition. Any value _not_ present in the supplied
    `:atom` should provide an `:onChange` handler.

  - `:atom`: atom of key => initial value for schema entries. Any entry found in
     both `:atom` and in `:schema` will remain synchronized between the panel and
     the supplied `:atom`.

  - `:folder`: optional map with optional keys `:name` and `:settings`:

    - `:name`: if provided, these controls will be nested inside of a folder
      with this name.

    - `:settings`: optional map customizing the folder's settings.
      See [[folder]] for a description of the supported options.

  - `:store`: this is an advanced option that you probably won't need. If you
    _do_ need this, pass a store created via leva's `useCreateStore`."
  [opts]
  [:f> Controls* opts])
