(ns cark.keechma.behavior-tree.example.ui.spinner
  (:require [keechma.toolbox.css.core :refer-macros [defelement]]))

(defn render
  ([] (render nil))
  ([{:keys [style]}]
   [:div.loader {:style (merge {:position "static"
                                :display "inline-block"
                                :margin-right ".7em"
                                :width "1em"
                                :height "1em"}
                               style)}
    [:div.inner.one][:div.inner.two][:div.inner.three]]))
