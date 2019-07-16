(ns cark.keechma.behavior-tree.example.ui.header 
  (:require [reagent.core :as reagent]
            [keechma.ui-component :as ui]
            [keechma.toolbox.ui :refer [sub> route> <cmd]]
            [keechma.toolbox.util :refer [class-names]]
            [keechma.toolbox.css.core :refer-macros [defelement]]
            [cark.keechma.behavior-tree.example.stylesheets.colors :as c]))

(defn log [value]
  (js/console.log value)
  value)

(defelement -menu-label
  :tag :span
  :class ["label"]
  :style [{:border-width ".1em"
           :border-bottom-style "solid"
           :border-bottom-color "transparent"
           :transition-property "border-color"
           :transition-duration ".5s"
           :transition-timing-function "easeInOutQuart"
           :white-space "nowrap"}])

(defelement -menu-item
  :tag :li
  :style [{:padding-left "0em"
           :padding-right "0em"
           :list-style "none"
           :flex-grow  "1"
           :display "inline-block"
           :text-align "center"
           :cursor "pointer"}
          [:&:hover {}
           [:.label {:border-bottom-color (:silver-d c/colors-with-variations)}]]
          [:&.selected {}
           [:.label {:border-bottom-color (:white c/colors)}]]])

(defelement -menu
  :tag :ul
  :style [{:display         "flex"
           :flex-grow       "2"
           :flex-direction  "row"
           :flex-wrap       "nowrap"
           :justify-content "space-between"
           :padding-left    "0px"
           :border-color    "red"
           :align-items     "baseline"
           :font-size ".8em"}])

(defn menu-item [ctx label page]
  (let [id [::menu-item page]
        current-route (route> ctx)
        selected? (= page (:page current-route))]
    [-menu-item {:class (class-names {:selected selected?}) 
                 :on-click #(when (not selected?)
                              (ui/redirect ctx {:page page}))}     
     [-menu-label
      label]]))

(defn header-menu [ctx]
  [-menu
   [menu-item ctx "Traffic lights" "traffic-lights"]
   [menu-item ctx "Database restore" "database-restore"]]) 

(defelement -version-label
  :tag :span
  :style [{:padding-right ".5em"}])

(defelement -version-value
  :tag :span
  :style [{:color     (:white c/colors)
           :font-size "80%"}])

(defelement -version
  :tag :div
  :style [{:padding       ".3em"
           :padding-right "1em"
           :font-size     "80%"
           :color         (:silver c/colors-with-variations)
           :flex-grow     "0"}])

(defn version [ctx]
  [-version
   [-version-label
    "ver:"]
   [-version-value
    "0.0.0"]])

(defelement -toast
  :tag :h1
  :style [{:padding      ".1em"
           :padding-left "0em"
           :margin       ".4em"
           :font-size    "1.2em"
           :display      "inline-block"
           :flex-grow    "1"
           :white-space  "nowrap"}])

(defelement -header
  :tag :div
  :style [{:background      (str "linear-gradient(" (:primary c/colors) "," (:primary-d c/colors-with-variations)") ")
           :color           (:white c/colors)
           :margin          "0em"
           :padding         "0em"
           :padding-left    "2em"
           :padding-right   "2em"
           :display         "flex" 
           :flex-direction  "row"
           :flex-wrap       "nowrap"
           :justify-content "space-between"
           :align-items     "baseline"
           :font-size       "130%"}])

(defelement -spacer
  :tag :span
  :style [{:flex-grow "5"}])

(defn render [ctx]
  [-header 
   [-toast "Behavior-tree.example"]
   [header-menu ctx]
   [-spacer]
   [version ctx]])

(def component 
  (ui/constructor
   {:renderer render
    :subscription-deps []
    :component-deps []
    :topic :user-actions}))
