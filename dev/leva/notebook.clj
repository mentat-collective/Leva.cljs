^#:nextjournal.clerk
{:toc true
 :no-cache true
 :visibility :hide-ns}
(ns leva.notebook
  (:require [mentat.clerk-utils.show :refer [show-sci]]))

;; # Leva.cljs
;;
;; A [React](https://reactjs.org/)
;; / [Reagent](https://reagent-project.github.io/) interface to
;; the [Leva](https://github.com/pmndrs/leva/) GUI library.

;; [![Build Status](https://github.com/mentat-collective/leva.cljs/actions/workflows/kondo.yml/badge.svg?branch=main)](https://github.com/mentat-collective/leva.cljs/actions/workflows/kondo.yml)
;; [![License](https://img.shields.io/badge/license-MIT-brightgreen.svg)](https://github.com/mentat-collective/leva.cljs/blob/main/LICENSE)
;; [![cljdoc badge](https://cljdoc.org/badge/org.mentat/leva.cljs)](https://cljdoc.org/d/org.mentat/leva.cljs/CURRENT)
;; [![Clojars Project](https://img.shields.io/clojars/v/org.mentat/leva.cljs.svg)](https://clojars.org/org.mentat/leva.cljs)
;;
;; > The interactive documentation on this page was generated from [this source
;; > file](https://github.com/mentat-collective/leva.cljs/blob/$GIT_SHA/dev/leva/notebook.clj)
;; > using [Clerk](https://github.com/nextjournal/clerk). Follow
;; > the [instructions in the
;; > README](https://github.com/mentat-collective/leva.cljs/tree/main#interactive-documentation-via-clerk)
;; > to run and modify this notebook on your machine!
;; >
;; > See the [Github
;; > project](https://github.com/mentat-collective/leva.cljs) for more
;; > details, and the [cljdoc
;; > page](https://cljdoc.org/d/org.mentat/leva.cljs/CURRENT/doc/readme) for
;; > detailed API documentation.
;;
;; ## What is Leva?
;;
;; Good question!
;;
;; ## In Progress Demos
;;
;;
;; ### Config

(show-sci
 [leva/Config
  {:titleBar
   {:drag true
    :position {:x 0 :y 30}}}])

;; ### Sync with Server
;;
;; Note the Leva panel in the top right. The state is bi-directionally bound to
;; TWO atoms, one of which syncs.



;; Then add the panel and see it on the right. `leva/Controls` sends things to the
;; global panel, and they accumulate.

(show-sci
 (def !synced
   (reagent/atom
    {:number 10
     :cake {:r 10 :g 12}
     :string "Hi!"}))

 [:<>
  [leva/Controls
   {:folder {:name "Client / Server Sync"}
    :atom !synced
    :schema {:point {:a 10 :b 100}}}]])

;; Try changing the values in the panel and hit `return` to update, or (for
;; numeric values) drag the slider on the left side of the input box. Hold down
;; `option/alt` to drag with small steps or `shift` to drag with big steps.
;;
;; Note that the server side state changes:

;; ### Bidirectional syncing
;;
;; Now let's change the atom on the client side. Note the panel can drag the
;; slider etc. They are tied!

(show-sci
 [:<>
  [:input
   {:type :range :min 0 :max 10 :step 1
    :value (:number @!synced)
    :on-change
    (fn [target]
      (let [v (.. target -target -value)]
        (swap! !synced assoc :number (js/parseInt v))))}]
  [:pre (str @!synced)]])

;; ## Stacking Global Options

(show-sci
 (reagent/with-let
   [!local (reagent/atom
            {:point {:x 10 :y 12}})]
   [:<>
    [leva/Controls
     {:folder {:name "Local State"}
      :atom !local}]
    [:pre (str @!local)]]))

;; ## Special Inputs
;;
;; No atom necessary!
(show-sci
 [leva/Controls
  {:folder {:name "Special Inputs"}
   :schema
   {"Yellow Subfolder"
    (leva/folder
     {:button (leva/button js/alert)
      :group  (leva/button-group
               {"1px" #(js/alert "1px")
                "2px" #(js/alert "2px")})
      :monitor (leva/monitor
                (fn []
                  (let [t (js/Date.now)]
                    (Math/sin (/ t 300))))
                {:graph true
                 :interval 30})}
     {:color "yellow"})}}])

;; ### SubPanel

;; make an inline panel. This does NOT use a global store. You should set `:fill
;; true` to get it to fill its inline.

(show-sci
 (reagent/with-let
   [!state (reagent/atom {:face 12})]
   [:<>
    [leva/SubPanel {:fill true
                    :titleBar
                    {:drag false}}
     [leva/Controls {:atom !state}]]
    [:pre (str @!state)]]))

;; ### Conditional toggling

(show-sci
 (reagent/with-let
   [!show  (reagent/atom {:show true})
    !local (reagent/atom
            {:point {:x 10 :y 12}})]
   [:<>
    [leva/SubPanel {:fill true
                    :titleBar
                    {:drag false}}
     [leva/Controls
      {:folder {:name "Local State"}
       :atom   !show
       :schema {:show
                {:label "Show remaining?"
                 :order -1}}}]
     (when (:show @!show)
       [leva/Controls
        {:folder {:name "Local State"}
         :atom !local}])]
    [:pre (str @!local)]]))

;; ### Schema vs State
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

;;
;; You can get more control by passing in a schema...

;; ### No atoms
;;
;; ### Input Types

;; Customize the panel:
;; https://github.com/pmndrs/leva/blob/main/docs/configuration.md, see storybook
;; for more options

;; TODO maybe do leva-busy for fun. https://codesandbox.io/s/github/pmndrs/leva/tree/main/demo/src/sandboxes/leva-busy?file=/src/App.tsx:3276-3281

;; Here are all of the possible inputs: https://github.com/pmndrs/leva/blob/main/packages/leva/src/types/public.ts#L130-L142

;; type SchemaItem =
;; | InputWithSettings<number, NumberSettings>
;; | InputWithSettings<boolean>
;; | InputWithSettings<string>
;; | IntervalInput
;; | ColorVectorInput
;; | Vector2dInput
;; | Vector3dInput
;; | ImageInput
;; | SelectInput
;; | BooleanInput
;; | StringInput
;; | CustomInput<unknown>

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

;; NOTE: if it's a special input... we pass it along, no changes.
;;
;; all of the other LevaInputs can synchronize, no problem.
;;
;; What about the custom inputs? Maybe we say that for anything beyond the
;; basics, you have to manually deal with those yourself... but maybe not, maybe
;; if it has a value, then onChange can synchronize.
;;
;; NOTE: I think no state is fine if you have onChange handlers for everyone.
;; But if you are missing one AND don't provide an atom you get an error.
;;
;; EITHERRRRR you set value and onChange... or you let the atom handle those. If
;; you have anyone with no value and onChange AND AN ATOM you fail.
;;
;; NOTE are there default values for these? can I skip "value", like if you
;; don't want to pull it from the atom?

;; ### TODO NON-reactive atoms work too
;;
;; ### TODO use cursors to control only some sub-piece of the state atom
;;
;; ### TODO controlling order

;; https://github.com/pmndrs/leva/pull/394

;;
;; ## Folders
;;
;; note the settings

;; TODO test that this CAN work if I want to test it out.
;; NOTE make a note that there is no guarantee this will work well.
#_["@leva-ui/plugin-plot" :as p]

;; There are more demos that live here
;; https://github.com/pmndrs/leva/tree/main/demo/src/sandboxes, and we can
;; access them with the same URL.
;;
;; For plugins, here is an example:
;; https://github.com/pmndrs/leva/tree/main/packages/plugin-plot
;;
;; TODO maybe add links to the sandboxes in the notebook?
;;
;; TODO test a custom input. I THINK these need onChange handlers too, always,
;; or else they create re-renders. And this is part of the spec.... SO HANDLE
;; IT!!!
;;
;; NOTE I added the code for this, but we have to test that this actually makes
;; sense.

;; ## Thanks and Support

;; To support this work and my other open source projects, consider sponsoring
;; me via my [GitHub Sponsors page](https://github.com/sponsors/sritchie). Thank
;; you to my current sponsors!

;; I'm grateful to [Clojurists Together](https://www.clojuriststogether.org/)
;; for financial support during this library's creation. Please
;; consider [becoming a member](https://www.clojuriststogether.org/developers/)
;; to support this work and projects like it.
;;
;; For more information on me and my work, visit https://samritchie.io.

;; ## License

;; Copyright © 2022 Sam Ritchie.

;; Distributed under the [MIT
;; License](https://github.com/mentat-collective/leva.cljs/blob/main/LICENSE).
;; See [LICENSE](https://github.com/mentat-collective/leva.cljs/blob/main/LICENSE).
