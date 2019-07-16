(ns cark.keechma.behavior-tree.example.ui.panel
  (:require [reagent.core :as reagent]
            [keechma.ui-component :as ui]
            [keechma.toolbox.ui :refer [sub> route> <cmd]]
            [keechma.toolbox.css.core :refer-macros [defelement]]
            [cark.keechma.behavior-tree.example.stylesheets.colors :as c]))

(def border-color "#DDD")

(defelement -panel
  :tag :div
  :style [{:display "inline-block"
           :border-radius ".2em"
           :box-shadow (str ".05em .05em .1em .1em " border-color)}])

(defelement -title-div
  :tag :div
  :style [{:padding ".5em"
           :padding-left "1em"
           :border-bottom (str "1px solid " border-color)
           :font-weight "bold"}
          [:&.warning {:color (:warning c/colors)}]
          [:&.danger {:color (:danger c/colors)}]
          [:&.primary {:color (:primary c/colors)}]
          [:&.info {:color (:info c/colors)}]
          [:&.success {:color (:success c/colors)}]])

(defelement -content-div
  :tag :div
  :style [{:padding "1em"}])

(defn render 
  ([title content]
   [render {} title content])
  ([{:keys [class style]} title content]
   [-panel {:style style}
    [-title-div {:class (if class
                          (if (keyword? class)
                            (name class)
                            class)
                          "primary")}
     title]
    [-content-div content]]))

