(ns leva.schema
  (:require ["leva" :as l]
            [leva.types :as t]
            [goog.object :as o]))

(defn on-change-fn
  "Given an atom, returns a function from key->onChange."
  [!state]
  (fn k->on-change [key]
    (fn on-change [value _ _]
      (let [value (if (t/primitive? value)
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

                     (t/primitive? entry)
                     (do (js/console.error
                          (str "Primitives not allowed in schema definition. Use an entry in the atom: "
                               k ", " entry))
                         acc)

                     (vector? entry)
                     (do (js/console.error
                          (str "Vectors not allowed in schema definition. Use an entry in the atom: "
                               k ", " entry))
                         acc)

                     (t/custom-input? entry)
                     (do (when-let [v (get state k)]
                           (ignore k v "schema has custom input."))
                         (insert! acc k entry))

                     (t/folder? entry)
                     (do (when-let [v (get state k)]
                           (ignore k v "schema registered as folder."))
                         (insert! acc k (l/folder
                                         (rec (:schema entry))
                                         (clj->js
                                          (:settings entry)))))

                     (t/special-input? entry)
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
