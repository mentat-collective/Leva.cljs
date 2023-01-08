^#:nextjournal.clerk
{:toc true
 :no-cache true
 :visibility :hide-ns
 :auto-expand-results? true}
(ns leva.notebook
  (:require [mentat.clerk-utils.show :refer [show-sci]]))

;; # Leva.cljs
;;
;; A [Reagent](https://reagent-project.github.io/) interface to
;; the [Leva](https://github.com/pmndrs/leva/) declarative GUI library.

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
;; [Leva](https://github.com/pmndrs/leva) is a JavaScript library for building
;; declarative GUIs with many different input types, like the one hovering on
;; the right side of this page. Supported inputs range from numeric sliders and
;; color pickers to complex plugins like this [Bezier curve
;; example](https://leva.pmnd.rs/?path=/story/plugins-bezier--default-bezier).
;;
;; [Leva.cljs](https://github.com/mentat-collective/leva.cljs) extends Leva with
;; a set of [Reagent](https://reagent-project.github.io/) components that make
;; it easy to synchronize the state of the GUI with an [ClojureScript
;; atom](https://clojure.org/reference/atoms).
;;
;; Think of your GUI like an interactive, beautiful view onto your page's state.
;;
;; ## Quickstart
;;
;; Install `Leva.cljs` into your Clojurescript project using the instructions at
;; its Clojars page:

;; [![Clojars
;;    Project](https://img.shields.io/clojars/v/org.mentat/leva.cljs.svg)](https://clojars.org/org.mentat/leva.cljs)
;;
;; Or grab the most recent code using a Git dependency:
;;
;; ```clj
;; ;; deps
;; {io.github.mentat-collective/leva.cljs
;;   {:git/sha "$GIT_SHA"}}
;; ```

;; Require `leva.core` in your namespace:

;; ```clj
;; (ns my-app
;;   (:require [leva.core :as leva]
;;             [reagent.core :as reagent]))
;; ```

;; Declare some state that you'd like to control with a GUI.

(show-sci
 (defonce !synced
   (reagent/atom
    {:number 10
     :color {:r 10 :g 12 :b 4}
     :string "Hi!"
     :point {:x 1 :y 1}})))

;; Pass the atom to the `leva/Controls` component to add its entries to the Leva
;; panel hovering on the right, and bidirectionally bind its state to the
;; interactive state in the panel:

(show-sci
 [leva/Controls
  {:folder {:name "Quickstart"}
   :atom !synced}])

;; Drag the control panel around to a more convenient place on the page, then
;; play around with the UI elements and watch the state change:

(show-sci
 [v/inspect @!synced])

;; > If you're not familiar with React or Reagent, or what a "component" is,
;; > please give the [Reagent homepage](https://reagent-project.github.io/) a
;; > read. If this is your first Clojurescript experience, come say hi to
;; > me (@sritchie) in the [Clojurians Slack](http://clojurians.net/) and I'll
;; > get you started.

;; ## Guides
;;
;; ### Config
;;
;; TODO show how to configure a global panel's options.

;; Customize the panel:
;; https://github.com/pmndrs/leva/blob/main/docs/configuration.md, see storybook
;; for more options

(show-sci
 [leva/Config
  {:titleBar
   {:drag true
    :position {:x 0 :y 30}}}])

;; ### Customizing via Schema
;;
;; TODO also note that options stack up in the panel.

(show-sci
 (reagent/with-let
   [!local (reagent/atom
            {:point {:x 10 :y 12}})]
   [:<>
    [leva/Controls
     {:folder {:name "More State"}
      :atom !local}]
    [:pre (str @!local)]]))

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

;; ### State Management

;; #### Syncing via Atom
;;
;; TODO note that obviously we've already covered this, but this is what is
;; going on.
;;
;; TODO Actually this is cool, show that we can change the atom and the UI changes too.
;;
;; Note the Leva panel in the top right. The state is bi-directionally bound to
;; the atom.

;; Then add the panel and see it on the right. `leva/Controls` sends things to the
;; global panel, and they accumulate.

;; Try changing the values in the panel and hit `return` to update, or (for
;; numeric values) drag the slider on the left side of the input box. Hold down
;; `option/alt` to drag with small steps or `shift` to drag with big steps.
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

;; The remaining examples will use this style.

;; #### explicit onChange
;;
;; TODO you can also give value and onChange.

;; value and onValue syncing

;; ### All Input Types

;; TODO show off the full range.

;; Here are all of the possible inputs: https://github.com/pmndrs/leva/blob/main/packages/leva/src/types/public.ts#L130-L142

;; | ImageInput
;;  NOTE {:image <thing>} with settings mashed in

;; | ColorVectorInput
;; NOTE primitive form == map of specific kv pairs. CHECK FOR THESE EXACT ONES since if they don't match we fall back to vectors!!

;; type ColorRgbaInput = { r: number; g: number; b: number; a?: number }
;; type ColorHslaInput = { h: number; s: number; l: number; a?: number }
;; type ColorHsvaInput = { h: number; s: number; v: number; a?: number }
;; export type ColorVectorInput = ColorRgbaInput | ColorHslaInput | ColorHsvaInput

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

;; ### Special Inputs
;;
;; No atom necessary!
(show-sci
 [leva/SubPanel {:fill true :titleBar {:drag false}}
  [leva/Controls
   {:folder {:name "Special Inputs"}
    :schema
    {"Yellow Subfolder"
     (leva/folder
      {:button (leva/button js/alert)
       :group  (leva/button-group
                {"1px" #(js/alert "1px")
                 "2px" #(js/alert "2px")})
       "Cake (r)"
       (leva/monitor
        (fn []
          (let [t (js/Date.now)]
            (Math/sin (/ t 300))))
        {:graph true
         :interval 30})}
      {:color "yellow"})}}]])

;; ### Schema vs State
;;
;; show how to customize with a schema.

;; ### TODO folder options

;; ## Advanced Guides

;; ### Conditional toggling
;;
;; TODO I should actually do this with the `:render` keyword.

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

;; TODO scan for more goodies from storybook
;; https://leva.pmnd.rs/?path=/story/inputs-string--simple

;; document other inputs https://github.com/pmndrs/leva/blob/main/docs/inputs.md

;; TODO maybe do leva-busy for fun. https://codesandbox.io/s/github/pmndrs/leva/tree/main/demo/src/sandboxes/leva-busy?file=/src/App.tsx:3276-3281

;; ### TODO Cursors for State
;;
;; ### TODO controlling order

;; https://github.com/pmndrs/leva/pull/394

;; ### TODO Plugins

;; test that this CAN work if I want to test it out.
;; NOTE make a note that there is no guarantee this will work well.
#_["@leva-ui/plugin-plot" :as p]

;; TODO test a custom input. I THINK these need onChange handlers too, always,
;; or else they create re-renders. And this is part of the spec.... SO HANDLE
;; IT!!!


;; NOTE I added the code for this, but we have to test that this actually makes
;; sense.

;; ### TODO Resources from Leva

;; plugin list etc

;; There are more demos that live here
;; https://github.com/pmndrs/leva/tree/main/demo/src/sandboxes, and we can
;; access them with the same URL.
;;
;; For plugins, here is an example:
;; https://github.com/pmndrs/leva/tree/main/packages/plugin-plot
;;
;; TODO maybe add links to the sandboxes in the notebook?
;;
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
