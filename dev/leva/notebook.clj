^#:nextjournal.clerk
{:toc true
 :no-cache true
 :visibility :hide-ns}
(ns leva.notebook
  {:nextjournal.clerk/auto-expand-results? true}
  (:require [mentat.clerk-utils.docs :as docs]
            [mentat.clerk-utils.show :refer [show-sci]]
            [nextjournal.clerk :as clerk]))

^{::clerk/visibility {:code :hide :result :hide}}
(clerk/eval-cljs
 ;; These aliases only apply inside this namespace.
 '(require '[leva.core :as leva])
 '(require '[reagent.core :as reagent]))

;; # Leva.cljs
;;
;; A [Reagent](https://reagent-project.github.io/) interface to
;; the [Leva](https://github.com/pmndrs/leva/) declarative GUI library.

;; [![Build Status](https://github.com/mentat-collective/leva.cljs/actions/workflows/kondo.yml/badge.svg?branch=main)](https://github.com/mentat-collective/leva.cljs/actions/workflows/kondo.yml)
;; [![License](https://img.shields.io/badge/license-MIT-brightgreen.svg)](https://github.com/mentat-collective/leva.cljs/blob/main/LICENSE)
;; [![cljdoc badge](https://cljdoc.org/badge/org.mentat/leva.cljs)](https://cljdoc.org/d/org.mentat/leva.cljs/CURRENT)
;; [![Clojars Project](https://img.shields.io/clojars/v/org.mentat/leva.cljs.svg)](https://clojars.org/org.mentat/leva.cljs)
;;
;; > The interactive documentation on this page was generated
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
;; it easy to synchronize the state of the GUI with a [ClojureScript
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

^{::clerk/visibility {:code :hide}}
(docs/git-dependency
 "mentat-collective/leva.cljs")

;; Require `leva.core` in your ClojureScript namespace:

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
 [nextjournal.clerk.viewer/inspect @!synced])

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
;; global Leva panel. The following example sets the title of the panel and
;; marks it as draggable (the default):

(show-sci
 [leva/Config
  {:titleBar
   {:drag true
    :title "Drag Me!"}}])

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
;; This section contains usage examples for all of the standard inputs available
;; to Leva. To tighten up the examples, we'll define a Reagent component here
;; that accepts an initial state and, optionally, a schema for customizing that
;; state.
;;
;; The component will generate a subpanel pinned to the page.

(show-sci
 (defn ControlDemo
   ([initial-state]
    (ControlDemo initial-state {}))
   ([initial-state schema]
    (reagent/with-let
      [!state (reagent/atom initial-state)]
      [:div {:style {:width "60%" :margin "auto"}}
       [leva/SubPanel {:fill true :titleBar {:drag false}}
        [leva/Controls
         {:atom !state
          :schema schema}]]
       [nextjournal.clerk.viewer/inspect @!state]]))))

;; > Some of the following examples show off inputs using `:schema` entries only
;; > without an associated `:atom`. This is just to keep the examples tighter.
;; > You'll want to use an `:atom` or `:onChange`, as described above in [State
;; > Management](#state-management).

;; #### Number

;; Adjust numerical inputs with the up/down arrow keys or by dragging on the
;; small "v" icon on the left side of the input. Hold down the alt/option or
;; shift keys to slow or speed up the rate of change.

;; Providing an explicit `:step` will override the auto-calculated step size.

;; The `:suffix` option coerces the value to a string; the slider will change
;; the value preceding the suffix inside the string.

(show-sci
 [ControlDemo
  {:number 10
   :stepped 10
   :width "10px"}

  {:stepped {:step 0.25}
   :width
   {:suffix "px"
    :label "with suffix"}}])

;; #### Boolean

;; Boolean values resolve to a toggle.

(show-sci
 [ControlDemo
  {:checkbox true}])

;; #### String
;;
;; String values generate text input fields. See the component below for
;; explanations of the various customization options available.

(show-sci
 [ControlDemo
  {:string "Hit enter after editing!"
   :multiple-lines "Leva also supports <textarea/>\nAllowing for\nmultiple lines"
   :fixed-rows "You can specify the number of rows you need."
   :read-only "This text is not editable but still supports\nline\nbreaks."}

  {:string {:order -1}
   :multiple-lines {:rows true}
   :fixed-rows {:rows 3}
   :read-only {:editable false}}])

;; #### Image / File Upload

;; Setting a map-shaped value with an `:image` key will insert a button that
;; triggers a file upload. Initialize your value with `{:image nil}`.

;; On a successful upload, the value will change to `{:image <blob-url>}`, where
;; `blob-url` is a `URL` that references
;; a [`Blob`](https://developer.mozilla.org/en-US/docs/Web/API/Blob/Blob).
;;
;; Upload an image below and see the resulting state and uploaded image appear
;; on the page.

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

;; Leva provides many different ways to specify a color. All of these inputs
;; resolve to a color picker. Specifying an alpha channel will insert an extra
;; alpha slider at the bottom of the color picker.
;;
;; Expand the state below the panel and adjust the pickers to see state updates
;; for each color style.

(show-sci
 [ControlDemo
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
   :Hsva { :h 58 :s 92 :v 100 :a 0.3 }}])

;; #### 2D Vector

;; Create a 2D vector input by specifying either a 2-element vector or map with
;; two key-value pairs. Each coordinate has its own [Number](#number)-style
;; input. The dot to the left of each coordinate is a joystick. Click and drag
;; to open a small plane you can use to select both coordinates at once.

;; > Due to Leva's [duck typing](https://en.wikipedia.org/wiki/Duck_typing) of
;; > elements, be careful not to specify key names like `:image`, or else Leva
;; > will ignore your second key-value pair and create an Image input.

;; This example also shows how to configure either style of vector input by
;; removing its joystick, inverting the usual up-is-negative direction of the
;; `y` coordinate input, or adding an aspect ratio lock with `:lock true`.

;; With a locked vector, changing one coordinate with the slider will change the
;; other coordinate's value to keep the same aspect ratio.

(show-sci
 [ControlDemo
  {:vector-style [1 2]
   :map-style {:x 1 :y 2}
   :with-lock [1 2]
   :no-joystick [1 2]
   :inverted [1 2]}

  {:vector-style {:order -2}
   :map-style {:order -1}
   :with-lock {:lock true}
   :no-joystick {:joystick false}
   :inverted {:joystick "invertY"}}])

;; #### 3D Vector

;; Similar to [2D vector](#2d-vector) with a third coordinate and no joystick.
;;
;; When using the map form, make sure not to use a key named `:image`, or any of
;; the keysets from the [Color Picker](#color-picker) input, like `:r`, `:g` and
;; `:b`.

(show-sci
 [ControlDemo
  {:vector-style [1 2 3]
   :map-style {:x 1 :y 2 :z 3}}])

;; #### Interval
;;
;; An interval is defined with a 2-element vector, along with schema entries for
;; `:min` and `:max`. (Without `:min` and `:max` Leva will infer a [2D
;; vector](#2d-vector).)
;;
;; Adjust either number with the slider, or type new values into the numeric
;; inputs.
;;
;; Entering a value outside of `:min` or `:max` will truncate the value into the
;; correct range.

(show-sci
 [ControlDemo
  {:interval [1 2]}

  {:interval
   {:min 0 :max 10}}])


;; #### Select

;; The selector allows you to choose some predefined object from a dropdown of
;; choices. Specify any object you like for your value, and provide a vector or
;; map of options via an `:options` key in your schema.
;;
;; If you provide a map, the keys will be used as labels and the values as your
;; choices. For a vector, the dropdown will use the string representations of
;; each choice.
;;
;; If your initial value is not present in `:choices` it will be appended to the
;; list of possible options.

;; > Note that all non-function choices round-trip through Leva, so any Clojure
;; > data type like a set or keyword won't survive. Keywords become strings,
;; > sets become vectors, etc.

(show-sci
 [ControlDemo
  {:selected-value "cake"
   :custom-labels "hello"}

  {:selected-value
   {:options
    ["cake" "bananas" "margarine"]}

   :custom-labels
   {:options
    {"Hello World" "hello"
     "CAR and CDR" "lisp"
     "Vector" [1 2]
     "Function as Value" (fn [] "cake")}}}])

;; ### Special Inputs

;; Special inputs are panel elements like buttons or folders that aren't
;; typical "inputs" like those in [Standard Inputs](#standard-inputs)
;; above.
;;
;; These all come with special constructors
;; in [`leva.core`](https://github.com/mentat-collective/leva.cljs/blob/main/src/leva/core.cljs).
;; See
;;  [these constructors in `leva.core`](https://github.com/mentat-collective/leva.cljs/blob/main/src/leva/core.cljs) for documentation.

;; #### Folder
;;
;; `leva.core/folder` takes a sub-schema and, optionally, a map of settings.
;; Items in the sub-schema will be nested in a folder with name equal to the
;; folder's key in the schema.

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
;; `leva.core/button` takes an `on-click` handler and an optional map of
;; settings, and inserts a button into the schema labeled with the button's
;; schema key.

(show-sci
 [:div {:style {:width "60%" :margin "auto"}}
  [leva/SubPanel {:fill true :titleBar {:drag false}}
   [leva/Controls
    {:schema
     {:button
      (leva/button
       (fn []
         (js/alert "Button clicked!")))}}]]])

;; #### Button Group
;;
;; A Button group is a horizontal group of small buttons, each with an
;; associated `on-click` handler.
;;
;; Define a button group by calling `leva.core/button-group` with a map of label
;; => `on-click` (or, in the 2-arity case, a label and then the same map).

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

;; A monitor allows you to monitor repeated return values from some no-argument
;; function. Obviously the idea here is that this function internally queries
;; some other stateful value! Initialize a monitor by passing a no-arg function
;; to `leva.core/monitor` along with (optionally) a map of settings.
;;
;; > Note that you can also pass a React `MutableRefObject` as returned
;; > by [`useRef`](https://reactjs.org/docs/hooks-reference.html#useref),
;; > where `(.-current ref)` returns a number. The monitor will then monitor the
;; > value of the ref's `current` property.
;;
;; Supported settings are

;; - `:graph`: if `true`, the returned monitor shows a graph. if `false`, the
;; monitor displays a number.
;; - `:interval`: the number of milliseconds to wait between queries of the
;;   `ref` or function

;; Adjust the `:number` slider below and watch the graphical output change:

(show-sci
 (reagent/with-let
   [!local (reagent/atom {:number 7})]
   [:div {:style {:width "60%" :margin "auto"}}
    [leva/SubPanel {:fill true :titleBar {:drag false}}
     [leva/Controls
      {:folder {:name "Monitor Demo"}
       :atom !local
       :schema
       {:number {:order -1}
        "Number Monitor"
        (leva/monitor
         (fn []
           (:number @!local))
         {:graph true
          :interval 30})}}]]]))

;; ### Custom Plugins

;; Leva is extensible, and has support for a number
;; of [plugins](https://github.com/pmndrs/leva/tree/main/packages). Plugins by
;; default will _not_ synchronize with your `:atom`; you'll have to provide
;; an [explicit onChange](#explicit-onchange) to capture state (which you can of
;; course `swap!` into an atom!)
;;
;; > Once [this issue](https://github.com/mentat-collective/leva.cljs/issues/2)
;; > is resolved this limitation will no longer exist.
;;
;; Leva comes bundled with the following plugins:

;; - [Bezier curve](https://github.com/pmndrs/leva/tree/main/packages/plugin-bezier) ([storybook examples]((https://leva.pmnd.rs/?path=/story/plugins-bezier)))
;; - [Date picker](https://github.com/pmndrs/leva/tree/main/packages/plugin-dates) ([storybook examples]((https://leva.pmnd.rs/?path=/story/plugins-dates)))
;; - [Function plotter](https://github.com/pmndrs/leva/tree/main/packages/plugin-plot) ([storybook examples]((https://leva.pmnd.rs/?path=/story/plugins-plot)))
;; - [Spring simulation](https://github.com/pmndrs/leva/tree/main/packages/plugin-sprin) ([storybook examples]((https://leva.pmnd.rs/?path=/story/plugins-spring)))

;; The following demo shows off the Bezier curve plugin.

;; #### Bezier curve

;; First, install the plugin with:

;; ```sh
;; npm install @leva-ui/plugin-bezier
;; ```

;; > See the links above for the package to install for your plugin of actual
;; > interest.

;; Then add the package to your namespace:

;; ```clj
;; (ns my-app
;;   (:require ["@leva-ui/plugin-bezier" :as b]
;;             [leva.core :as leva]
;;             [reagent.core :as reagent]))
;; ```

;; Each plugin exposes a function that you can use to generate a schema entry.
;; Because custom plugins can't (yet,
;; see [##2](https://github.com/mentat-collective/leva.cljs/issues/2))
;; synchronize with your atom automatically, you'll need to provide an
;; `:onChange` function to retrieve new values.

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
    [nextjournal.clerk.viewer/inspect @!local]]))

;; Supported options are the union
;; of [`InputOptions`](https://github.com/pmndrs/leva/blob/main/packages/leva/src/types/public.ts#L182-L188)
;; and any option mentioned in the plugin's documentation.
;;
;; If documentation is poor, your best bet is to follow the `storybook examples`
;; link referenced above, and then track down the associated `*.stories.tsx`
;; file associated with the plugin to see what code generated the story.

;; Here is the source for the [Bezier
;; stories](https://github.com/pmndrs/leva/blob/main/packages/plugin-bezier/src/Bezier.stories.tsx),
;; for example.

;; ## Leva.cljs via SCI
;;
;; `Leva.cljs` is compatible with [SCI, the Small Clojure
;; Interpreter](https://github.com/babashka/sci).
;;
;; To install `Leva.cljs` into your SCI context, require
;; the [`leva.sci`](https://cljdoc.org/d/org.mentat/leva.cljs/CURRENT/api/leva.sci)
;; namespace and call `leva.sci/install!`:

;; ```clj
;; (ns myproject.sci-extensions
;;   (:require [leva.sci]))

;; (leva.sci/install!)
;; ```
;;
;; If you want more granular control, see the [cljdoc page for
;; `leva.sci`](https://cljdoc.org/d/org.mentat/leva.cljs/CURRENT/api/leva.sci)
;; for an SCI config and distinct SCI namespace objects that you can piece
;; together.
;;
;; > Note that `Leva.cljs` does not ship with a dependency on SCI, so you'll
;; > need to install your own version.
;;
;; ## Leva.cljs via Clerk
;;
;; Using `Leva.cljs` with Nextjournal's [Clerk](https://clerk.vision/) gives you
;; the ability to write notebooks like this one with embedded GUI controls.
;;
;; Doing this requires that you generate a custom ClojureScript build for your
;; Clerk project. The easiest way to do this for an existing project is with
;; the [`clerk-utils` project](https://clerk-utils.mentat.org/). Follow the
;; instructions on the [`clerk-utils` guide for custom
;; ClojureScript](https://clerk-utils.mentat.org/#custom-clojurescript-builds).
;;
;; If this is your first time using Clerk, use the [`leva/clerk` template
;; described below](#project-template) to generate a new project with all steps
;; described in ["Leva.cljs via SCI"](#leva.cljs-via-sci) already completed.
;;
;; ## Project Template
;;
;; `Leva.cljs` includes
;; a [`deps-new`](https://github.com/seancorfield/deps-new) template called
;; [`leva/clerk`](https://github.com/mentat-collective/clerk-utils/tree/main/resources/clerk_utils/custom)
;; that makes it easy to configure a new Clerk project with everything described
;; in ["Leva.cljs via SCI"](#leva.cljs-via-sci) already configured.

;; First, install the [`deps-new`](https://github.com/seancorfield/deps-new) tool:

;; ```sh
;; clojure -Ttools install io.github.seancorfield/deps-new '{:git/tag "v0.5.0"}' :as new
;; ```

;; To create a new Clerk project based on
;; [`leva/clerk`](https://github.com/mentat-collective/leva.cljs/tree/main/resources/leva/clerk)
;; in a folder called `my-notebook-project`, run the following command:

^{::clerk/visibility {:code :hide}}
(clerk/md
 (format "
```sh
clojure -Sdeps '{:deps {io.github.mentat-collective/leva.cljs {:git/sha \"%s\"}}}' \\
-Tnew create \\
:template leva/clerk \\
:name myusername/my-notebook-project
```" (docs/git-sha)))

;; The README.md file in the generated project contains information on how to
;; develop within the new project.

;; If you have an existing Clerk notebook project and are considering adding
;; `Leva.cljs`, you might consider
;; using [`leva/clerk`](https://github.com/mentat-collective/leva.cljs/tree/main/resources/leva/clerk)
;; to get some ideas on how to structure your own project.

;; ## Advanced Guides
;;
;; These guides cover usage patterns that don't fit with any particular input
;; type, but are useful to know about.

;; ### Conditional toggling
;;
;; All input elements support a `:render` option. Provide a no-argument
;; function; if this function returns `true`, the component will render. If
;; `false`, the component will remain hidden.
;;
;; The following example conditionally shows a 2D vector input based on the
;; state of a checkbox:

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
    [nextjournal.clerk.viewer/inspect @!local]]))

;; ### Cursors for State

;; `:atom` can take anything atom-like, not just ClojureScript or Reagent atoms.
;; For example, if you want to synchronize a panel with some subset of a larger
;; piece of state, you might pass a [Reagent
;; cursor](https://github.com/reagent-project/reagent/blob/master/doc/ManagingState.md#cursors).
;;
;; This example renders the current state of a cursor synchronized with a 2D
;; vector input, and the full state of its parent atom below it:

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
    [nextjournal.clerk.viewer/inspect @!state]]))
;;
;; ### Input Ordering

;; Because ClojureScript's maps aren't ordered, you might find that your panel
;; is showing your inputs in some order other than the one you declared in you
;; `:schema` or `:atom` inputs.
;;
;; Every input supports an `:order` key that you can use to provide a number for
;; relative ordering (0 by default).
;;
;; This example forces the ordering of 3 inputs with `:order` entries:

(show-sci
 [:div {:style {:width "60%" :margin "auto"}}
  [leva/SubPanel {:fill true :titleBar {:drag false}}
   [leva/Controls
    {:schema
     {:x {:value 10
          :label "I'm last"
          :order 2}
      :y {:value 10
          :label "I'm first"
          :order -1}
      :z {:value 10
          :label "In the middle"
          :order 0}}}]]])

;; ### Resources from Leva
;;
;; Leva is a big library, and the documentation above doesn't come close to
;; covering all possible options and use cases for all input types.
;;
;; Here are some more resources available about Leva:

;; - [Storybook](https://leva.pmnd.rs/)
;; - [Github](https://github.com/pmndrs/leva)

;; #### Code Sandboxes
;;
;; These sandboxes provide implementations of more complex panels meant to show
;; off use cases like panel theming, more advanced styling and the different
;; plugins available.

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

;; ## Who is using Leva.cljs?

;; The following projects use Leva.cljs:

;; - [MathBox.cljs](https://mathbox.mentat.org)

;; If you want to show off your use of Leva.cljs, please [file a
;; ticket](https://github.com/mentat-collective/leva.cljs/issues/new) and let us
;; know!

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

;; Copyright © 2022-2023 Sam Ritchie.

;; Distributed under the [MIT
;; License](https://github.com/mentat-collective/leva.cljs/blob/main/LICENSE).
;; See [LICENSE](https://github.com/mentat-collective/leva.cljs/blob/main/LICENSE).
