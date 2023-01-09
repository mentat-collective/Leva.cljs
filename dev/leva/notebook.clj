^#:nextjournal.clerk
{:toc true
 :no-cache true
 :visibility :hide-ns}
(ns leva.notebook
  {:nextjournal.clerk/auto-expand-results? true}
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

;; Declare some state that you'd like to control with a GUI. Each entry's key
;; becomes its label, and Leva infers the correct input from the value's type.

(show-sci
 (defonce !synced
   (reagent/atom
    {:number 10
     :color {:r 10 :g 12 :b 4}
     :string "Hi!"
     :point {:x 1 :y 1}})))

;; Pass the atom to the `leva/Controls` component via the `:atom` key to add its
;; entries to the Leva panel hovering on the right, and bidirectionally bind its
;; state to the interactive state in the panel:

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
;; Leva.cljs exposes three main components:
;;
;; - `leva/Config`, for configuring the global panel
;; - `leva/SubPanel`, for declaring separate panels with their own internal
;;   stores
;; - `leva/Controls`, for adding inputs to either type of panel.
;;
;; ### Panel Configuration
;;
;; `leva/Config` takes a map of options and applies them as settings to the
;; global Leva panel. The following example adjusts the initial position of the
;; panel downward:

(show-sci
 [leva/Config
  {:titleBar
   {:drag true
    :position {:x 0 :y 30}}}])

;; See the
;; type [`LevaRootProps`](https://github.com/pmndrs/leva/blob/main/packages/leva/src/components/Leva/LevaRoot.tsx#L13-L93)
;; for a full list of available entries for `opts` and documentation for each.

;; You can place the `leva/Config` instance anywhere in your render tree, as it
;; won't actually insert anything visual into the DOM.

;; ### Adding More Inputs
;;
;; The `leva/Controls` component in [Quickstart](#quickstart) above added a
;; number of inputs to the global panel. Successive `leva/Controls` instances
;; will append inputs to that panel while keeping the state separate for each
;; group of inputs.

(show-sci
 (reagent/with-let
   [!local (reagent/atom
            {:point [2 3 4]})]
   [:<>
    [leva/Controls
     {:folder {:name "Adding More Inputs"}
      :atom !local}]
    [:pre (str @!local)]]))

;; > See [Standard Inputs](#standard-inputs) and [Special
;; > Inputs](#special-inputs) below for a guide to all supported input types.

;; Specifying the same folder name will append items to that folder:

(show-sci
 (reagent/with-let
   [!local (reagent/atom
            {:slider 10})]
   [:<>
    [leva/Controls
     {:folder {:name "Adding More Inputs"}
      :atom   !local
      :schema {:slider
               {:label "Slide Me!"
                :min 0 :max 12}}}]
    [:pre (str @!local)]]))

;; Note that both `:schema` and `:atom` here share a slider key. See the
;; following section on [Schema](#schema) for more detail.

;; ### Schema
;;
;; The first examples added inputs via the `:atom` key, but this most recent
;; example specified a `:schema`.
;;
;; All input types support configuration options that don't belong in the
;; synchronized state. Pass these to `leva/Controls` using the `:schema` option.
;;
;; If a key is present in both the `:atom` and the `:schema`, the value in
;; `:atom` will win, but all settings from the `:schema` will apply.
;;
;; A schema entry with _no_ corresponding `:atom` entry will emit a warning if
;; you don't provide an `:onChange` handler to capture changes in the input's
;; value. See [explicit onChange](#explicit-onchange) below for more detail.

;; ### SubPanel

;; `leva/SubPanel` declares a separate local panel with its own internal store.
;; These panels take the same options described in [Panel
;; Configuration](#panel-configuration).

;; By default, `SubPanel` instances will visually stack on top of the global
;; instance. Pass `:fill true` to have the `SubPanel` instead stick to the page
;; and fill its parent DOM element.
;;
;; Any `Controls` instance nested as a child will render its controls into the
;; `SubPanel` vs the global panel:

(show-sci
 [:div {:style {:width "60%" :margin "auto"}}
  [leva/SubPanel {:fill true
                  :titleBar
                  {:drag false}}
   [leva/Controls
    {:schema {:color "blue"
              :range
              {:value [4 5] :min 0 :max 10}}}]]])

;; > This example passes a `:schema` with no `:atom`! This is useful for
;; > visually prototyping a panel, but useless for getting any information out
;; > of the panel. You'll see warnings in the terminal if you do this.

;; ### State Management
;;
;; Leva panels can provide state updates either by binding to an atom or through
;; callbacks specified via `:onChange` entries in the schema.

;; #### Syncing via Atom
;;
;; As in [Quickstart](#quickstart), if you configure a panel by passing an atom
;; to `Controls` via the `:atom` key, Leva will generate input elements that
;; bind bi-directionally to your atom's state.

;; All of the examples above have used this style, as it's the most natural way
;; to deal with state in a ClojureScript application.

;; If you change the atom's value from outside, the Leva panel will update. Play
;; with the slider below to change the `:number` key specified
;; in [Quickstart](#quickstart) and watch the value in the global panel change.
;; Change the panel value and see the slider update.

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

;; #### Explicit onChange

;; You can also configure panel elements by providing `:value` and `:onChange`
;; entries in the `:schema`. This example provides an initial value for a
;; `range` input via `:value`, and uses an `:onChange` handler to store a string
;; in a local reactive atom.

(show-sci
 (reagent/with-let
   [!local (reagent/atom "Not yet set!")]
   [:div {:style {:width "60%" :margin "auto"}}
    [leva/SubPanel {:fill true
                    :titleBar
                    {:drag false}}
     [leva/Controls
      {:schema
       {:range
        {:value [4 5]
         :min 0
         :max 10
         :onChange
         (fn [v]
           (reset! !local (str "The range's value is " (pr-str v) ".")))}}}]]
    [:pre @!local]]))

;; > NOTE if your input type is a map, like a color specified via `{:r
;; > <number> :g <number> :b <number>}`, add these entries directly to your
;; > schema instead of nesting them under `:value`.

;; ### Standard Inputs
;;
;; > Some of the following examples show off inputs using `:schema` entries only
;; > without an associated `:atom`. This is just to keep the examples tighter.
;; > You'll want to use an `:atom` or `:onChange`, as described above in [State
;; > Management](#state-management).
;;
;; #### Number

(show-sci
 (reagent/with-let
   [!local (reagent/atom
            {:number 10})]
   [:div {:style {:width "60%" :margin "auto"}}
    [leva/SubPanel {:fill true :titleBar {:drag false}}
     [leva/Controls
      {:atom !local}]]
    [:pre (str @!local)]]))

;; #### Boolean

(show-sci
 (reagent/with-let
   [!local (reagent/atom
            {:checkbox true})]
   [:div {:style {:width "60%" :margin "auto"}}
    [leva/SubPanel {:fill true :titleBar {:drag false}}
     [leva/Controls
      {:atom !local}]]
    [:pre (str @!local)]]))

;; #### String

(show-sci
 (reagent/with-let
   [!local (reagent/atom
            {:string "Hit enter after editing!"})]
   [:div {:style {:width "60%" :margin "auto"}}
    [leva/SubPanel {:fill true :titleBar {:drag false}}
     [leva/Controls
      {:atom !local}]]
    [:pre (str @!local)]]))

;; #### Image

;; To get an image, specify a map with an `:image` key as your value.

(show-sci
 (reagent/with-let
   [!local (reagent/atom
            {:image {:image nil}})]
   [:div {:style {:width "80%" :margin "auto"}}
    [leva/SubPanel {:fill true :titleBar {:drag false}}
     [leva/Controls
      {:atom !local}]]
    [:pre (str @!local)]
    (if-let [image (:image @!local)]
      [:img {:src image
             :height "300px"}]
      [:p "Upload an image to see it here!"])]))

;; #### Color Picker

;; This example shows all possible color input styles.

(show-sci
 (reagent/with-let
   [!local (reagent/atom
            {:Name "royalblue"
             :Hex "#9442ff"
             :Hex8 "#8b33ffaa"
             :RgbString "rgb(255, 47, 162)"
             :RgbaString "rgba(233, 30, 99, 0.9)"
             :Rgb {:r 0 :g 150 :b 136 }
             :Rgba {:r 139 :g 195 :b 74 :a 0.5}
             :Hsl {:h 4 :s 90 :l 58}
             :Hsla {:h 36 :s 100 :l 50 :a 1}
             :HslString "hsl(199, 98%, 48%)"
             :HslaString "hsla(187, 1%, 42%, 0.9)"
             :Hsv { :h 238 :s 100 :v 70 }
             :Hsva { :h 58 :s 92 :v 100 :a 0.3 }})]
   [:div {:style {:width "60%" :margin "auto"}}
    [leva/SubPanel {:fill true :titleBar {:drag false}}
     [leva/Controls
      {:atom !local}]]
    [v/inspect @!local]]))

;; #### 2D Vector

;; Specify these with either a 2-element vector or a map. If you have a map with
;; two keys, the system infers vector2d.

(show-sci
 (reagent/with-let
   [!local (reagent/atom
            {:vector-style [1 2]
             :map-style {:x 1 :y 2}})]
   [:div {:style {:width "80%" :margin "auto"}}
    [leva/SubPanel {:fill true :titleBar {:drag false}}
     [leva/Controls
      {:atom !local}]]
    [v/inspect @!local]]))

;; #### 3D Vector

;; Similar to 2d vector but without a joystick.

;; Specify these with either a 2-element vector or a map. If you have a map with
;; two keys, the system infers vector2d... UNLESS you pick three of the keys
;; from the [Color Picker](#color-picker) above, or `:image`.

(show-sci
 (reagent/with-let
   [!local (reagent/atom
            {:vector-style [1 2 3]
             :map-style {:x 1 :y 2 :z 3}})]
   [:div {:style {:width "80%" :margin "auto"}}
    [leva/SubPanel {:fill true :titleBar {:drag false}}
     [leva/Controls
      {:atom !local}]]
    [v/inspect @!local]]))

;; #### Interval
;;
;; Note that here we need to use `:schema` along with `:atom` to provide
;; options. Otherwise our `[1 2]` is inferred to be a [2d vector](#2d-vector).

(show-sci
 (reagent/with-let
   [!local (reagent/atom
            {:interval [1 2]})]
   [:div {:style {:width "60%" :margin "auto"}}
    [leva/SubPanel {:fill true :titleBar {:drag false}}
     [leva/Controls
      {:atom !local
       :schema {:interval
                {:min 0 :max 10}}}]]
    [:pre (str @!local)]]))


;; #### Select

;; If your schema contains `:options`, then ANY value you have in your state map
;; will be interpreted as a choice and rendered as a dropdown.

(show-sci
 (reagent/with-let
   [!local (reagent/atom
            {:selected-value "cake"})]
   [:div {:style {:width "60%" :margin "auto"}}
    [leva/SubPanel {:fill true :titleBar {:drag false}}
     [leva/Controls
      {:atom !local
       :schema {:selected-value
                {:options
                 ["cake" "bananas" "margarine"]}}}]]
    [:pre (str @!local)]]))

;; #### Custom Inputs

;; This example shows off a bezier curve. You'll have to install this with:

;; ```sh
;; npm install @leva-ui/plugin-bezier
;; ```

;; ```clj
;; (ns my-app
;;   (:require ["@leva-ui/plugin-bezier" :as b]
;;             [leva.core :as leva]
;;             [reagent.core :as reagent]))
;; ```

;; Then here is an example:

(show-sci
 (reagent/with-let
   [!local (reagent/atom nil)]
   [:div {:style {:width "60%" :margin "auto"}}
    [leva/SubPanel {:fill true :titleBar {:drag false}}
     [leva/Controls
      {:schema
       {:curve
        (b/bezier
         {:value    [0.54, 0.05, 0.6, 0.98]
          :onChange (fn [v] (reset! !local v))})}}]]
    [v/inspect @!local]]))

;; The options supported here
;; are [`InputOptions`](https://github.com/pmndrs/leva/blob/main/packages/leva/src/types/public.ts#L182-L188)
;; plus whatever input the plugin supports. Unfortunately these don't seem well
;; documented... soyou'll have to look at the [Stories
;; page](https://leva.pmnd.rs/?path=/story/plugins-bezier--default-bezier), and
;; usually go find the associated stories page in the repo. Here are the [Bezier
;; stories](https://github.com/pmndrs/leva/blob/main/packages/plugin-bezier/src/Bezier.stories.tsx),
;; for example.

;; ### Special Inputs

;; These all come with special constructors in `leva.core`.

;; None of these do any synchronization with the atom, so you have to work
;; through their interfaces. See the constructors for documentation.

;; #### Folder
;;
;; `leva/folder` allows you to nest subfolders.

(show-sci
 [:div {:style {:width "60%" :margin "auto"}}
  [leva/SubPanel {:fill true :titleBar {:drag false}}
   [leva/Controls
    {:folder {:name "Outer Folder"}
     :schema
     {"Yellow Subfolder"
      (leva/folder
       {:button (leva/button
                 (fn []
                   (js/alert
                    "Button pressed!")))
        :group  (leva/button-group
                 {"1px" #(js/alert "1px")
                  "2px" #(js/alert "2px")})}
       {:color "yellow"})}}]]])

;; #### Button
;;
;; Buttons don't sync with atoms; provide a no-arg click handler to the
;; `leva/button` constructor.

(show-sci
 [:div {:style {:width "60%" :margin "auto"}}
  [leva/SubPanel {:fill true :titleBar {:drag false}}
   [leva/Controls
    {:schema
     {:button (leva/button
               (fn []
                 (js/alert "Button clicked!")))}}]]])

;; #### Button Group
;;
;; Defines a group of buttons.

(show-sci
 (reagent/with-let
   [!local (reagent/atom {:number 7})]
   [:div {:style {:width "60%" :margin "auto"}}
    [leva/SubPanel {:fill true :titleBar {:drag false}}
     [leva/Controls
      {:schema
       {:group
        (leva/button-group
         "Meal Choice"
         {"Meat" #(js/alert "Meat!")
          "Potatoes" #(js/alert "Potatoes!")
          "Cabbage" #(js/alert "Cabbage!")})}}]]]))

;; #### Monitor

;; Monitor a no-arg function. Slide or change the `:number` slider and watch the
;; monitor value respond.

(show-sci
 (reagent/with-let
   [!local (reagent/atom {:number 7})]
   [:div {:style {:width "60%" :margin "auto"}}
    [leva/SubPanel {:fill true :titleBar {:drag false}}
     [leva/Controls
      {:folder {:name "Special Inputs"}
       :atom !local
       :schema
       {:number {:order -1}
        "Number Monitor"
        (leva/monitor
         (fn []
           (:number @!local))
         {:graph true
          :interval 30})}}]]]))

;; ## Advanced Guides

;; ### Conditional toggling

(show-sci
 (reagent/with-let
   [!local (reagent/atom
            {:show true
             :point {:x 10 :y 12}})]
   [:div {:style {:width "60%" :margin "auto"}}
    [leva/SubPanel {:fill true
                    :titleBar
                    {:drag false}}
     [leva/Controls
      {:folder {:name "Local State"}
       :atom   !local
       :schema {:show
                {:label "show point?"}
                :point
                {:render
                 (fn [] (:show @!local))}}}]]
    [:pre (str @!local)]]))

;; ### Cursors for State

;; Use a Reagent cursor if you want to pin your UI to some sub-piece of larger
;; state.

(show-sci
 (reagent/with-let
   [!state (reagent/atom
            {:more "entries"
             :that "you want to ignore"
             :gui {:point [1 2]}})
    !cursor (reagent/cursor !state [:gui])]
   [:div {:style {:width "60%" :margin "auto"}}
    [leva/SubPanel {:fill true
                    :titleBar
                    {:drag false}}
     [leva/Controls {:atom !cursor}]]
    [:pre (str @!cursor)]
    [v/inspect @!state]]))
;;
;; ### Input Ordering

;; The schema and `:atom` maps aren't ordered, so you can use `:order` keys to
;; set a relative ordering.

(show-sci
 [:div {:style {:width "60%" :margin "auto"}}
  [leva/SubPanel {:fill true :titleBar {:drag false}}
   [leva/Controls
    {:schema {:x {:value 10
                  :label "I'm last"
                  :order 2}
              :y {:value 10
                  :label "I'm first"
                  :order -1}
              :z {:value 10
                  :label "In the middle"
                  :order 0}}}]]])

;; ### Resources from Leva

;; - [Storybook](https://leva.pmnd.rs/)
;; - [Github](https://github.com/pmndrs/leva)

;; #### Code Sandboxes

;; - [leva-advanced-panels](https://codesandbox.io/s/github/pmndrs/leva/tree/main/demo/src/sandboxes/leva-advanced-panels?file=/src/App.tsx)
;; - [leva-busy](https://codesandbox.io/s/github/pmndrs/leva/tree/main/demo/src/sandboxes/leva-busy?file=/src/App.tsx)
;; - [leva-custom-plugin](https://codesandbox.io/s/github/pmndrs/leva/tree/main/demo/src/sandboxes/leva-custom-plugin?file=/src/App.tsx)
;; - [leva-minimal](https://codesandbox.io/s/github/pmndrs/leva/tree/main/demo/src/sandboxes/leva-minimal?file=/src/App.tsx)
;; - [leva-plugin-bezier](https://codesandbox.io/s/github/pmndrs/leva/tree/main/demo/src/sandboxes/leva-plugin-bezier?file=/src/App.tsx)
;; - [leva-plugin-dates](https://codesandbox.io/s/github/pmndrs/leva/tree/main/demo/src/sandboxes/leva-plugin-dates?file=/src/App.tsx)
;; - [leva-plugin-plot](https://codesandbox.io/s/github/pmndrs/leva/tree/main/demo/src/sandboxes/leva-plugin-plot?file=/src/App.tsx)
;; - [leva-plugin-spring](https://codesandbox.io/s/github/pmndrs/leva/tree/main/demo/src/sandboxes/leva-plugin-spring?file=/src/App.tsx)
;; - [leva-scroll](https://codesandbox.io/s/github/pmndrs/leva/tree/main/demo/src/sandboxes/leva-scroll?file=/src/App.tsx)
;; - [leva-theme](https://codesandbox.io/s/github/pmndrs/leva/tree/main/demo/src/sandboxes/leva-theme?file=/src/App.tsx)
;; - [leva-transient](https://codesandbox.io/s/github/pmndrs/leva/tree/main/demo/src/sandboxes/leva-transient?file=/src/App.tsx)

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
