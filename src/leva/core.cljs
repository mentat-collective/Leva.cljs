(ns leva.core
  "Interface for Leva."
  (:require ["leva" :as l]
            ["react" :as react]
            [goog.object :as o]))

;; ## Types and Schema Predicates

(def FolderType
  (.-type (l/folder #js {} #js {})))

(def SpecialInputs
  {:button       (.-type (l/button (fn []) #js {}))
   :button-group (.-type (l/buttonGroup nil))
   :monitor      (.-type (l/monitor (fn []) #js {}))})

(def SpecialInputTypes
  (into #{} (vals SpecialInputs)))

(def primitive?
  (some-fn number? boolean? string?))

(defn folder? [entry]
  (= FolderType (:type entry)))

(defn special-input? [entry]
  (contains? SpecialInputTypes (:type entry)))

(defn custom-input?
  "Returns true if we have a custom input, false otherwise. JS objects since you
  use their constructor.

  NOTE that these will be JS objects since they're built with the constructor
  over there."
  [entry]
  (boolean
   (o/get entry "__specialInput")))

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
     {:type (:button SpecialInputs)
      :onClick on-click
      :settings (merge defaults settings)})))

(defn button-group
  "Relevant type for opts: https://github.com/pmndrs/leva/blob/33b2d9948818c5828409e3cf65baed4c7492276a/packages/leva/src/types/public.ts#L55-L64"
  [opts]
  {:type (:button-group SpecialInputs)
   :opts opts})

(defn monitor
  "Relevant type for opts: https://github.com/pmndrs/leva/blob/33b2d9948818c5828409e3cf65baed4c7492276a/packages/leva/src/types/public.ts#L71-L77

  Tons of stuff with a monitor demo:
  https://codesandbox.io/s/github/pmndrs/leva/tree/main/demo/src/sandboxes/leva-busy

  The monitor is going to call a thunk for us that checks on something."
  ([object-or-fn]
   (monitor object-or-fn {}))
  ([object-or-fn settings]
   {:type (:monitor SpecialInputs)
    :objectOrFn object-or-fn
    :settings settings}))

(defn folder
  "Example: https://github.com/pmndrs/leva/blob/33b2d9948818c5828409e3cf65baed4c7492276a/packages/leva/stories/Folder.stories.tsx#L71

  Key is the folder name, value is this

  settings: https://github.com/pmndrs/leva/blob/33b2d9948818c5828409e3cf65baed4c7492276a/packages/leva/src/types/public.ts#L81-L87"
  ([schema]
   (folder schema {}))
  ([schema settings]
   {:type FolderType
    :schema schema
    :settings settings}))

;; ## Configuration

(defn on-change-fn
  "Given an atom, returns a function from key->onChange."
  [!state]
  (fn k->on-change [key]
    (fn on-change [value _ _]
      (let [value (if (primitive? value)
                    value
                    (js->clj value :keywordize-keys true))]
        (when (not= value (get (.-state !state) key ::not-found))
          (swap! !state assoc key value))))))

(defn ^:no-doc controlled->js
  "This is way simpler.

  If v is a map, it gets merged into the schema. Otherwise
  it's added as `:value`... and we give a good warning."
  [k v schema k->on-change]
  (let [m (-> (if (map? v)
                (merge schema v)
                (assoc schema :value v))
              (assoc :onChange (k->on-change k)))]
    (when-let [bumped (keys (select-keys schema (keys m)))]
      (js/console.warn
       "Schema entry for " k " matches an entry in the `:atom`. "
       "The following keys are being evicted: "
       bumped))
    (clj->js m)))

(defn ^:no-doc uncontrolled->js
  "Uncontrolled is REQUIRED to have the goods."
  [k schema]
  (let [m (if (contains? schema :onChange)
            schema
            (do (js/console.warn
                 (str "no onChange for uncontrolled "
                      k "! Swapping in a no-op `:onChange`."))
                (assoc schema :onChange (fn [_]))))]
    (clj->js m)))

(defn ^:no-doc set-controlled-entries!
  "Set all remaining schemaless entries from the initial state."
  [acc m k->on-change]
  (letfn [(process [acc k v]
            (doto acc
              (o/set
               (name k)
               (controlled->js k v {} k->on-change))))]
    (reduce-kv process acc m)))

(defn ^:no-doc build-schema
  "Given a schema and an initial state, plus a way to make on onChange that
  propagates updates, builds a JS schema."
  [schema state k->on-change]
  (let [seen (atom #{})]
    (letfn [(ignore [k v message]
              (swap! seen conj k)
              (js/console.warn (str "ignoring " k ", " v " in  atom; " message)))

            (insert! [acc k v]
              (swap! seen conj k)
              (doto acc (o/set (name k) v)))
            (rec [schema]
              (reduce-kv
               (fn [acc k entry]
                 (let [entry (or entry {})]
                   (cond
                     (= "" (name k))
                     (do (js/console.error
                          (str "Keys can not be empty, if you want to hide a label use whitespace. Ignoring entry: "
                               k ", " entry))
                         acc)

                     (@seen k)
                     (do (js/console.error
                          (str "Duplicate key: " k ", ignoring entry: " entry))
                         acc)

                     (primitive? entry)
                     (do (js/console.error
                          (str "Primitives not allowed in schema definition. Use an entry in the atom: "
                               k ", " entry))
                         acc)

                     (vector? entry)
                     (do (js/console.error
                          (str "Vectors not allowed in schema definition. Use an entry in the atom: "
                               k ", " entry))
                         acc)

                     (custom-input? entry)
                     (do (when-let [v (get state k)]
                           (ignore k v "schema has custom input."))
                         (insert! acc k entry))

                     (folder? entry)
                     (do (when-let [v (get state k)]
                           (ignore k v "schema registered as folder."))
                         (insert! acc k (l/folder
                                         (rec (:schema entry))
                                         (clj->js
                                          (:settings entry)))))

                     (special-input? entry)
                     (do (when-let [v (get state k)]
                           (ignore k v "schema registered as special input."))
                         (insert! acc k (clj->js entry)))

                     (map? entry)
                     (if-let [v (get state k)]
                       (insert! acc k (controlled->js k entry v k->on-change))
                       (insert! acc k (uncontrolled->js k entry)))

                     :else
                     (do (js/console.error
                          (str "Unknown type " k ", " (pr-str entry) "; ignoring."))
                         acc))))
               (js-obj)
               schema))]
      (let [processed (rec schema)
            remaining (apply dissoc state @seen)]
        ;; now add in the keys from the atom that haven't been seen yet.
        (set-controlled-entries! processed remaining k->on-change)))))

(defn ^:no-doc opts->argv
  [{:keys [folder-name schema atom store folder-settings]}]
  (let [k->on-change  (on-change-fn atom)
        initial-state (.-state atom)
        ;; NOTE This function wrapper is required for `set` below to work. If
        ;; you don't want to synchronize state back from the atom, do this. See
        ;; below for more detail.
        schema        (fn []
                        (build-schema schema initial-state k->on-change))
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
        [_ set] (apply l/useControls (opts->argv opts))]
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
