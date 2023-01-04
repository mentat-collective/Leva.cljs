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

;; ## Types and Schema Predicates

(def FolderType
  (.-type (l/folder #js {} #js {})))

(def SpecialInputs
  {:button       (.-type (l/button (fn []) #js {}))
   :button-group (.-type (l/buttonGroup nil))
   :monitor      (.-type (l/monitor (fn []) #js {}))})

(def primitive?
  (some-fn number? boolean? string?))

(defn folder? [entry]
  (= FolderType (:type entry)))

(defn special-input? [entry]
  (contains? SpecialInputs (:type entry)))

(defn custom-input? [entry]
  ;; TODO make sure this works on a js object.
  (contains? entry "__specialInput"))

;; ## Input Constructors

(defn button
  "Relevant opts: https://github.com/pmndrs/leva/blob/33b2d9948818c5828409e3cf65baed4c7492276a/packages/leva/src/types/public.ts#L47-L53

  TODO what about render, z-order etc? Same for all of these below."
  [on-click settings]
  (let [defaults {:disabled false}]
    {:type (:button SpecialInputs)
     :onClick on-click
     :settings (merge defaults settings)}))

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
  [object-or-fn settings]
  {:type (:monitor SpecialInputs)
   :objectOrFn object-or-fn
   :settings settings})

(defn folder
  "Example: https://github.com/pmndrs/leva/blob/33b2d9948818c5828409e3cf65baed4c7492276a/packages/leva/stories/Folder.stories.tsx#L71

  Key is the folder name, value is the folder value...

  settings: https://github.com/pmndrs/leva/blob/33b2d9948818c5828409e3cf65baed4c7492276a/packages/leva/src/types/public.ts#L81-L87"
  [schema settings]
  {:type (:folder SpecialInputs)
   :schema schema
   :settings settings})

;; ## Configuration
;;
;; Customize the panel:
;; https://github.com/pmndrs/leva/blob/main/docs/configuration.md, see storybook
;; for more options

;; TODO we are currently walking the atom and building a schema. What we WANT TO
;; DO IS THIS:

;; - walk the schema, not the atom

;; - For updatable inputs, you can EITHER PROVIDE `value` and `onChange`, or it
;;   has to be present in the atom.
;;
;; - for things like buttons etc, we don't care.
;;
;; - Keep a set of keys that we've seen... finally, add to the schema the
;;   entries in the atom that don't have a schema entry, they need to count too.
;;   And if you want to keep some OUT, make a cursor.
;;
;; If you give `:value` and `:onChange` AND the atom, we can log a warning that
;; we are ignoring the atom.
;;
;; TODO maybe do leva-busy for fun. https://codesandbox.io/s/github/pmndrs/leva/tree/main/demo/src/sandboxes/leva-busy?file=/src/App.tsx:3276-3281

;; Here are all of the possible inputs: https://github.com/pmndrs/leva/blob/main/packages/leva/src/types/public.ts#L130-L142


(defn ^:no-doc build-schema
  "This has to work like https://github.com/pmndrs/leva/blob/33b2d9948818c5828409e3cf65baed4c7492276a/packages/leva/src/store.ts#L261-L296"
  [schema !state]
  (let [state (.-state !state)
        seen  (atom #{})]
    (letfn [(rec [schema]
              (reduce-kv
               (fn [acc key entry]
                 (let [k (name key)]
                   (cond (= "" k)
                         (js/console.error
                          (str "Keys can not be empty, if you want to hide a label use whitespace."))

                         (@seen key)
                         (js/console.error
                          (str "Duplicate key: " key))

                         (primitive? entry)
                         (js/console.error
                          (str "Primitives not allowed in schema definition. Use an entry in the atom."))

                         (vector? entry)
                         (js/console.error
                          (str "Vectors not allowed in schema definition. Use an entry in the atom."))

                         (custom-input? entry)
                         (doto acc
                           (o/set k entry))

                         (folder? entry)
                         (doto acc
                           (o/set k (l/folder
                                     ;; TODO deal with a warning and null on a recursive call.
                                     (rec (:schema entry))
                                     (clj->js
                                      (:settings entry)))))

                         ;; The rest of the special inputs don't synchronize state, so we don't need to do any checking against the atom.
                         (special-input? entry)
                         (doto acc
                           (o/set k (clj->js entry)))

                         ;; NOTE that this is what comes in
                         ;; OnChangeHandler = (value: any, path: string, context: OnChangeHandlerContext) => void

                         ;; InputOptions is a bunch of BS, but all we care about is onChange


                         ;; remaining types:
                         ;; | ImageInput
                         ;;  NOTE {:image <thing>} with settings mashed in

                         ;; | ColorVectorInput
                         ;; NOTE primitive form == map of specific kv pairs. CHECK FOR THESE EXACT ONES since if they don't match we fall back to vectors!!

                         ;; type ColorRgbaInput = { r: number; g: number; b: number; a?: number }
                         ;; type ColorHslaInput = { h: number; s: number; l: number; a?: number }
                         ;; type ColorHsvaInput = { h: number; s: number; v: number; a?: number }
                         ;; export type ColorVectorInput = ColorRgbaInput | ColorHslaInput | ColorHsvaInput

                         ;; TODO we need to handle non-primitive stuff coming in
                         ;; from onChange etc.

                         ;; NOTE :value, :onChange with other settings in there too
                         ;; | InputWithSettings<number, NumberSettings>
                         ;; | InputWithSettings<boolean>
                         ;; | InputWithSettings<string>
                         ;; | IntervalInput NOTE primitive form == [l r] with min max, either :value, :onChange
                         ;; TODO following ones are EITHER a map with 2/3 entries as value, OR a pair/triple.
                         ;; | Vector2dInput NOTE same, missing min max
                         ;; | Vector3dInput NOTE same as prev but with three


                         ;; | SelectInput ;; TODO ANYTHING with :options too. :value :onChange deal applies here.
                         ;; | DONE BooleanInput primitive onlyI
                         ;; | DONE StringInput since it's covered by the first thing above?
                         ;; | CustomInput<unknown>


                         ;; type SchemaItemWithOptions =
                         ;; | DONE number
                         ;; | DONE boolean
                         ;; | DONE string
                         ;; | (SchemaItem & InputOptions)
                         ;; | DONE (SpecialInput & GenericSchemaItemOptions)
                         ;; | DONE FolderInput<unknown>

                         ;; TODO ONLY MAPS ALLOWED, but fall through to here only after checking on others like image etc.
                         (map? entry)
                         ;; TODO this is close to working. But this only works
                         ;; for things with `:value` and `:onChange`... go
                         ;; through and figure out what constitutes a VALUE for
                         ;; the other ones.
                         ;;
                         ;;
                         (let [{:keys [value onChange]} entry]
                           (if (contains? state key)
                             (if (or value onChange)
                               (js/console.error
                                (str "State entry found for " key "; do NOT provide either `:value` or `:on-change`."))
                               (doto acc
                                 (o/set k (doto (clj->js entry)
                                            (o/set "value" value)
                                            (o/set "onChange"
                                                   (fn [value _ _]
                                                     ;; TODO if primitive, pass on, else clj->js
                                                     (when (not= value (get (.-state !state) key ::not-found))
                                                       (swap! !state assoc key value))))))))
                             (if (and value onChange)
                               (doto acc
                                 (o/set k (clj->js entry)))
                               (js/console.error
                                (str "There is no state entry for " key "; you must supply both :value and :onChange.")))))

                         ;; NOTE: if it's a special input... we pass it along, no changes.
                         ;;
                         ;; all of the other LevaInputs can synchronize, no problem.
                         ;;
                         ;; What about the custom inputs? Maybe we say that for
                         ;; anything beyond the basics, you have to manually
                         ;; deal with those yourself... but maybe not, maybe if
                         ;; it has a value, then onChange can synchronize.
                         ;;
                         ;; NOTE: I think no state is fine if you have onChange
                         ;; handlers for everyone. But if you are missing one
                         ;; AND don't provide an atom you get an error.
                         ;;
                         ;; EITHERRRRR you set value and onChange... or you let
                         ;; the atom handle those. If you have anyone with no
                         ;; value and onChange AND AN ATOM you fail.
                         ;;
                         ;; NOTE are there default values for these? can I
                         ;; skip "value", like if you don't want to pull it from
                         ;; the atom?
                         :else
                         (js/console.error
                          (str "Unknown type " k ", " (pr-str entry))))))
               (js-obj)
               schema))]
      (let [processed (rec schema)]
        ;; TODO now add in the keys from the atom that haven't been seen yet.
        processed))))

(defn ^:no-doc atom->schema-fn
  "I guess this is good if we have an atom and options. We also could just have a
  schema... but then how are they going to read the state back out? You need
  SOME way to deal with the handlers. I guess we can say, look, if you are using
  this library, you are going to communicate via an atom."
  ([!state] (atom->schema-fn !state {}))
  ([!state options]
   (fn []
     (reduce-kv
      (fn [acc k v]
        (letfn [(on-change [value _ _]
                  (js/console.log "incoming")
                  (js/console.log value)
                  (when (not= value (get (.-state !state) k ::not-found))
                    (swap! !state assoc k value)))]
          (doto acc
            (o/set
             (name k)
             ;; TODO Note that `k-opts` must be a map.
             ;;
             ;; TODO
             (if-let [k-opts (get options k nil)]
               (clj->js
                (assoc k-opts :value v :onChange on-change))
               #js {"value" v "onChange" on-change})))))
      (js-obj "banana" #js {"value" #js {:r 1 :g 1 :x 1} "onChange" (fn [value _ _]
                                                                      (js/console.log value))})
      (.-state !state)))))

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
                (fn []
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
