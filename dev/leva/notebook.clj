^#:nextjournal.clerk
{:toc true
 :no-cache true
 :visibility :hide-ns}
(ns leva.notebook
  (:require [mentat.clerk-utils :refer [cljs]]))

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
;; Note the Leva panel in the top right. The state is bi-directionally bound to
;; TWO atoms.
;;
;; Try changing the values in the panel and hit `return` to update, or (for
;; numeric values) drag the slider on the left side of the input box. Hold down
;; `option/alt` to drag with small steps or `shift` to drag with big steps.

^{:nextjournal.clerk/visibility {:code :fold}}
(cljs
 (reagent/with-let
   [!state1 (reagent/atom
             {:number 10 :string "face"})
    !state2 (reagent/atom {:cake 12})]
   [:<>
    [leva/PanelOptions {:drag true}]
    [leva/Panel {:state !state1}]
    [:input
     {:type :range :min 0 :max 10 :step 1
      :value (:number @!state1)
      :on-change
      (fn [target]
        (let [v (.. target -target -value)]
          (swap! !state1 assoc :number (js/parseInt v))))}]
    [:pre (str @!state1)]


    [leva/Panel {:state !state2}]
    [:input
     {:type :range :min 0 :max 10 :step 1
      :value (:cake @!state2)
      :on-change
      (fn [target]
        (let [v (.. target -target -value)]
          (swap! !state2 assoc :cake (js/parseInt v))))}]
    [:pre (str @!state2)]]))


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
