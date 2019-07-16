(ns cark.keechma.behavior-tree.example.ui.main
  (:require [reagent.core :as reagent]
            [keechma.ui-component :as ui]
            [keechma.toolbox.ui :refer [sub> route>]]
            [cark.keechma.behavior-tree.example.stylesheets.colors :as c] 
            [keechma.toolbox.css.core :refer-macros [defelement]]))

(defelement -page
  :tag :div
  :style [{:display "flex"
           :height "100%"
           :flex-direction "column"
           :justify-content "flex-start"}])


(defn render [ctx]
  (let [current-route (route> ctx)
        initialized? (sub> ctx :initialized?)] 
    (when initialized?
      [-page
       [(ui/component ctx :header)]
       (case (:page current-route)
         "traffic-lights" [(ui/component ctx :traffic-lights-page)]
         "database-restore" [(ui/component ctx :database-restore-page)])])))

(def component 
  (ui/constructor
   {:renderer render
    :subscription-deps [:initialized?]
    :component-deps [:header
                     :traffic-lights-page
                     :database-restore-page] 
    :topic :user-actions}))

