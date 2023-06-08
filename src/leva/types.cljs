(ns leva.types
  (:require ["leva" :as l]))

;; ## Types and Schema Predicates
;;
;; Leva component panels are defined by
;; a [schema](https://github.com/pmndrs/leva/blob/main/packages/leva/src/types/public.ts#L198);
;; Leva will try and infer the input that the user wants from the primitive,
;; vector or map in the schema.
;;
;; Some inputs types are inferred by checking for the presence of keys like
;; `:image`, `:r` `:g` `:b`, etc, while some are tagged with an explicit `:type`
;; keyword.
;;
;; This section binds the types that aren't explicitly exported by Leva's API,
;; and defines predicates for detecting schema types.
;;
;; (If we simply copied strings like "FOLDER" then the `:type` entries wouldn't
;; survive advanced compilation.)

(def ^{:doc "Internal type string used by Leva to tag folders."}
  FolderType
  (.-type (l/folder #js {} #js {})))

(def ^{:doc "Map of keyword to the internal type string used to declare each
  special input type."}
  SpecialInputs
  {:button (.-type (l/button (fn []) #js {}))
   :button-group (.-type (l/buttonGroup nil))
   :monitor      (.-type (l/monitor (fn []) #js {}))})

(def ^{:doc "Set of types"}
  SpecialInputTypes
  (into #{} (vals SpecialInputs)))

(def primitive?
  (some-fn number? boolean? string?))

(defn folder? [entry]
  (= FolderType (:type entry)))

(defn special-input? [entry]
  (contains? SpecialInputTypes (:type entry)))

(defn custom-input?
  "Returns true if we have a custom input, false otherwise."
  [entry]
  (contains? entry :__customInput))
