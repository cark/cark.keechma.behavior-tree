(ns cark.keechma.behavior-tree.example.ui.page
  (:require [reagent.core :as reagent]
            [keechma.ui-component :as ui]
            [keechma.toolbox.ui :refer [sub> route>]]
            [keechma.toolbox.css.core :refer-macros [defelement]]
            [cark.keechma.behavior-tree.example.stylesheets.colors :as c]))


(defelement -container
  :tag :div 
  :style {:display "grid"
          :grid-template-columns "15em auto 5em"
          :grid-template-rows "auto fr"
          :grid-template-areas
          " 
\" title-margin title title  \"
\" left-margin content right-margin \""
          })

(defelement -title
  :tag :div
  :style [{:grid-area :title}
          [:h2 {;:color "red"
                :margin "0px"
                :padding-top ".2em"
                :padding-bottom ".2em"
                :font-size "125%"
                :background-color (:white-d c/colors-with-variations)}]])

(defelement -title-margin
  :tag :div
  :style [{:grid-area :title-margin
           :background-color (:white-d c/colors-with-variations)}])

(defelement -content
  :tag :div
  :style {:grid-area :content
          :margin-left "1em"})

(defn render [{:keys [title content]}]
  [-container
   (when title
     [:<>
      [-title-margin]
      [-title [:h2 title]]])
   [-content content]])
