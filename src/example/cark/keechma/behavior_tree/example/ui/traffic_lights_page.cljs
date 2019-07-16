(ns cark.keechma.behavior-tree.example.ui.traffic-lights-page
  (:require [reagent.core :as reagent]
            [keechma.ui-component :as ui]
            [keechma.toolbox.ui :refer [sub> route>]]
            [cark.keechma.behavior-tree.example.stylesheets.colors :as c]
            [cark.keechma.behavior-tree.example.ui.page :as page]
            [keechma.toolbox.css.core :refer-macros [defelement]]
            [cark.keechma.behavior-tree.example.features.traffic-lights :as tl]))

(defn traffic-light [ctx size direction]
  (let [w 38
        half-w (/ w 2)
        h 80
        light-size (/ w 4.5)
        red "#D22"
        dim-red "#411"
        yellow "#FB2"
        dim-yellow "#431"
        green "#2D2"
        dim-green "#131"
        color (sub> ctx direction)]
    [:svg {:width (* w size) :height (* h size)
           :view-box (apply str (interpose " " [0 0 w h]))}   
     [:defs
      [:g {:id "tl-back" :fill "#000" }
       [:circle {:cx half-w :cy half-w :r half-w}]
       [:circle {:cx half-w :cy (- h half-w) :r half-w}]
       [:rect {:x 0 :y half-w :width w :height (- 80 w)}]]
      [:g {:id "light"}
       [:circle {:r light-size}]]]
     [:use {:x 0 :y 0 :href "#tl-back"}]
     [:use {:x half-w :y half-w :href "#light" :fill (if (= color :red) red dim-red)}]
     [:use {:x half-w :y (/ (+ half-w (- h half-w)) 2) :href "#light" :fill (if (= color :yellow) yellow dim-yellow)}]
     [:use {:x half-w :y (- h half-w) :href "#light" :fill (if (= color :green) green dim-green)}]]))

(defn crossroad [ctx size]
  (let [w 100
        half-w (/ w 2)
        h 100
        half-h (/ h 2)
        road-color "#CCC"]
    [:svg {:width "30em"
           :view-box (apply str (interpose " " [0 0 w h]))}
     [:defs
      [:g {:id "center" :fill road-color}
       [:rect {:x -11 :y -11 :width 22 :height 22}]]
      [:g {:id "stop-line" :fill "white"}
       [:rect {:x -5 :y -0.5 :width 10 :height 1 }]]
      [:g {:id "road" :fill road-color}
       [:rect {:x -10 :y -20 :width 20 :height 40}]
       [:use {:x 5 :y -10 :href "#stop-line"}]]
      [:g {:id "traffic-light1"}
       [traffic-light ctx 0.15 ::tl/north-south]]
      [:g {:id "traffic-light2"}
       [traffic-light ctx 0.15 ::tl/west-east]]]
     [:use {:x half-w :y half-h :href "#center"}]
     [:use {:x half-w :y 80 :href "#road"}]
     [:use {:href "#road" :transform "rotate(180) translate(-50,-20)"}]
     [:use {:href "#road" :transform "rotate(90) translate(50,-20)"}]
     [:use {:href "#road" :transform "rotate(270) translate(-50,80)"}]
     [:use {:x 37 :y 24 :href "#traffic-light1"}]
     [:use {:x 57 :y 64 :href "#traffic-light1"}]
     [:use {:x 67 :y 34 :href "#traffic-light2"}]
     [:use {:x 27.15 :y 54 :href "#traffic-light2"}]]))

(defn render [ctx]
  [page/render
   {:content [:div
              [:br]
              [crossroad ctx 4]]}]) 

(def component 
  (ui/constructor
   {:renderer render
    :subscription-deps [::tl/north-south
                        ::tl/west-east]
    :component-deps []
    :topic :traffic-lights})) 


