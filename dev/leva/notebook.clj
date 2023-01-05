^#:nextjournal.clerk
{:toc true
 :no-cache true
 :visibility :hide-ns}
(ns leva.notebook
  (:require [mentat.clerk-utils.show :refer [show-sci]]
            [nextjournal.clerk :as clerk]))

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
 [leva/GlobalConfig
  {:titleBar
   {:drag false
    :position {:x 0 :y 30}}}])

;; ### Sync with Server
;;
;; Note the Leva panel in the top right. The state is bi-directionally bound to
;; TWO atoms, one of which syncs.

^{::clerk/sync true}
(def !synced
  (atom
   {:number 10
    :string "Hi!"}))

;; Then add the panel and see it on the right. `leva/Panel` sends things to the
;; global panel, and they accumulate.

(show-sci
 [:<>
  [leva/Panel
   {:folder-name "Client / Server Sync"
    :state leva.notebook/!synced}]])

;; Try changing the values in the panel and hit `return` to update, or (for
;; numeric values) drag the slider on the left side of the input box. Hold down
;; `option/alt` to drag with small steps or `shift` to drag with big steps.
;;
;; Note that the server side state changes:

@!synced

;; ### Bidirectional syncing
;;
;; Now let's change the atom on the client side. Note the panel can drag the
;; slider etc. They are tied!

(show-sci
 [:<>
  [:input
   {:type :range :min 0 :max 10 :step 1
    :value (:number @leva.notebook/!synced)
    :on-change
    (fn [target]
      (let [v (.. target -target -value)]
        (swap! leva.notebook/!synced assoc :number (js/parseInt v))))}]
  [:pre (str @leva.notebook/!synced)]])

;; ## Stacking Global Options

(show-sci
 (reagent/with-let
   [!local (reagent/atom
            {:point {:x 10 :y 12}})]
   [:<>
    [leva/Panel
     {:folder-name "Local State"
      :state !local}]
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
     [leva/Panel {:state !state}]]
    [:pre (str @!state)]]))

;; ### Schema vs State
;;
;; You can get more control by passing in a schema...

;; ### No atoms
;;
;; ### Input Types

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

#_[leva/Panel
   {:folder-name "state 1"
    :folder-settings {:collapsed true}
    :state leva.notebook/!state1}]

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
