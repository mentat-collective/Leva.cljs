(ns leva.schema
  "Functions for converting a leva.cljs schema into a Leva schema set up for state
  synchronization via a ClojureScript atom."
  (:require ["leva" :as l]
            [leva.types :as t]
            [goog.object :as o]))

(defn on-change-fn
  "Given some atom `!state`, returns a function that accepts some `key` and
  returns a Leva OnChangeHandler that sets the entry in `!state` for `key` to
  the new incoming value."
  [!state]
  (if !state
    (fn k->on-change [k]
      (fn on-change [value _path _context]
        (let [state (.-state !state)
              v     (if (t/primitive? value)
                      value
                      (js->clj value :keywordize-keys true))]
          (when (not= v (get state k ::not-found))
            (swap! !state assoc k v)))))
    (fn [_k] (fn [_ _ _]))))

(defn controlled->js
  "Given

  - a key `k` and value `v` from some stateful store like an atom,
  - a `schema` for the value,
  - a state-updating function `on-change` of type (value, path, context) => void

  Returns a JS Leva schema entry that will configure the `k`'s value `v` to push
  state updates via on-change.

  NOTE that if `v` is a map, entries in `v` will take precedence over any
  duplicates in `schema`."
  [k v schema on-change]
  (let [m (-> (if (map? v)
                (merge schema v)
                (assoc schema :value v))
              (assoc :onChange on-change))]
    (when-let [evicted (keys (select-keys schema (keys m)))]
      (js/console.warn
       "Schema entry for " k " matches an entry in the `:atom`. "
       "The following keys are being evicted: "
       evicted))
    (clj->js m)))

(defn uncontrolled->js
  "Given some key `k` and a corresponding `schema`, returns `(clj->js schema)`
  after ensuring that `schema` has an onChange handler registered.

  If it doesn't, emits a warning and inserts a no-op handler before conversion.

  returns a JS Leva schema entry that will configure the `k`'s value `v` to push
  state updates via on-change.

  NOTE that we do this because without an `:onChange` handler, leva's
  `useControls` hook forces the component to re-render any time the value for
  `k` changes. The re-render is a waste because the user can't get at the
  changed value."
  [k schema]
  (let [m (if (contains? schema :onChange)
            schema
            (do (js/console.warn
                 (str "no `:onChange` for uncontrolled "
                      k "! Swapping in a no-op `:onChange`."))
                (assoc schema :onChange (fn [_ _ _]))))]
    (clj->js m)))

(defn merge-controlled-entries!
  "Given

  - a mutable JS object `acc` representing the Leva schema
  - a map `m` of new entries to merge into `acc`,
  - a function `k->on-change` from a `k` to a state-updating `on-change` handler,

  Sets `onChange` on all values in `m`.

  Returns `acc` with the transformed `m` merged in."
  [acc m k->on-change]
  (letfn [(process [acc k v]
            (doto acc
              (o/set
               (name k)
               (controlled->js k v {} (k->on-change k)))))]
    (reduce-kv process acc m)))

(defn normalize-entry
  "Accepts an `entry` in a leva schema and normalizes `nil?`, `vector?`
  or [[leva.types/primitive?]] values into a map for easier processing below.

  Given a map-shaped `entry`, acts as identity."
  [entry]
  (cond (nil? entry) {}

        (or (t/primitive? entry)
            (vector? entry))
        {:value entry}

        :else entry))

(defn build-schema
  "Given

  - a ClojureScript `schema`
  - an initial `state` (a dereferenced atom)
  - a function `k->on-change` from a `k` to a state-updating `on-change` handler,

  Returns a Leva schema (to pass to `useControls`) modified to synchronize state
  back to some external store like a Reagent atom."
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
                 (let [entry (normalize-entry entry)]
                   (cond
                     ;; This first block of cases covers invalid schema entries
                     ;; that we'd like to ignore.
                     (= "" (name k))
                     (do (js/console.error
                          (str "Ignoring empty key: "
                               k ". Keys can not be empty! If you want to hide a label, use whitespace."))
                         acc)

                     (@seen k)
                     (do (js/console.error
                          (str "Duplicate key: "
                               k ", ignoring appearance with value: "
                               entry))
                         acc)

                     ;; This next block covers cases where the schema contains
                     ;; some special input that we can't synchronize. We let the
                     ;; schema pass through and log a warning if the key is also
                     ;; present in the state map, since there is nothing to
                     ;; synchronize.
                     (t/folder? entry)
                     (do (when-let [v (get state k)]
                           (ignore k v (str k " is registered as a folder in the schema.")))
                         (insert! acc k (l/folder
                                         (rec (:schema entry))
                                         (clj->js
                                          (:settings entry)))))

                     (t/custom-input? entry)
                     (do (when-let [v (get state k)]
                           (ignore k v (str k " is registered as a custom input in the schema.")))
                         (insert! acc k (uncontrolled->js k entry)))

                     (t/special-input? entry)
                     (do (when-let [v (get state k)]
                           (ignore k v (str k " is registered as a special input in the schema.")))
                         (insert! acc k (clj->js entry)))

                     ;; These final cases are the bread-and-butter input
                     ;; definitions. If some input is specified in the state, we
                     ;; register an entry in the returned schema that will
                     ;; synchronize state back to the external store. Otherwise,
                     ;; we allow the schema to pass through after making sure it
                     ;; has a proper onChange handler (to prevent re-renders of
                     ;; the [[leva.core/Controls]] component).
                     (map? entry)
                     (if (contains? state k)
                       (let [on-change (k->on-change k)
                             v         (get state k)]
                         (insert! acc k (controlled->js k v entry on-change)))
                       (insert! acc k (uncontrolled->js k entry)))

                     :else
                     (do (js/console.error
                          (str "Unknown type " k ", " (pr-str entry) "; ignoring."))
                         acc))))
               (js-obj)
               schema))]
      (let [processed (rec schema)
            remaining (apply dissoc state @seen)]
        ;; Return the populated schema after adding in all state entries that
        ;; had no explicit schema definitions in `schema`.
        (doto processed
          (merge-controlled-entries! remaining k->on-change))))))

(defn opts->argv
  "Accepts the options map for a [[leva.core/Controls]] component and returns the
  rather confusing vector of arguments required by leva's `useControls` hook.

  The parsing logic [lives
  here](https://github.com/pmndrs/leva/blob/33b2d9948818c5828409e3cf65baed4c7492276a/packages/leva/src/useControls.ts#L30-L75)
  in leva."
  [{:keys [folder schema atom store]}]
  (let [k->on-change  (on-change-fn atom)
        initial-state (if atom (.-state atom) {})
        ;; NOTE This function wrapper is required for `set` to work
        ;; in [[leva.core/Controls]]. If you don't want to synchronize state
        ;; FROM the atom to leva, remove this. See [[leva.core/Controls]] for
        ;; more detail.
        schema        (fn []
                        (build-schema schema initial-state k->on-change))
        hook-settings (when store #js {:store store})]
    (if-let [folder-name (:name folder)]
      [folder-name
       schema
       (when-let [settings (:settings folder)]
         (clj->js settings))
       hook-settings]
      [schema hook-settings])))
