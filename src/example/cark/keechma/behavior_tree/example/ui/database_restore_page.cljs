(ns cark.keechma.behavior-tree.example.ui.database-restore-page
  (:require [reagent.core :as reagent]
            [keechma.ui-component :as ui]
            [keechma.toolbox.ui :refer [sub> route>]]
            [cark.keechma.behavior-tree.example.stylesheets.colors :as c]
            [cark.keechma.behavior-tree.example.ui.page :as page]
            [keechma.toolbox.css.core :refer-macros [defelement]]
            [cark.keechma.behavior-tree.example.features.database-restore :as drf]
            [cark.keechma.behavior-tree.controller :as btc]
            [cark.keechma.behavior-tree.example.ui.panel :as panel]
            [cark.keechma.behavior-tree.example.ui.spinner :as spinner]
            [oops.core :as oops]))

(defn log [value]
  (js/console.log value)
  value)

(defn read-file [ctx file]
  (let [reader (js/FileReader.)]    
    (doto reader
      (oops/oset! "onload" #(btc/<cmd-send-event ctx :got-file-data (oops/oget % "target.result")))
      (oops/ocall "readAsText" file))))

(defn open-file-dialog [ctx]
  (let [input (js/document.createElement "input")
        body (oops/oget js/document "body")
        input (doto input
                (oops/oset! "type" "file")
                (oops/oset! "accept" ".backup")
                (oops/oset! "onchange" #(when-let [file (aget (oops/oget input "files") 0)]
                                          (read-file ctx file))))]
    (doto input
      (oops/ocall "click")
      (oops/ocall "remove"))))

(defelement -content-group
  :tag :div
  :style {:margin-left "0.5em"
          :display :flex
          :flex-flow "row wrap"
          :justify-content :flex-start
          :align-items :flex-start})

(defelement -content-area
  :tag :div
  :style {:max-width "40em"
          :margin-left "1em"
          :margin-right "1em"
          :margin-bottom "1.5em"})

(defelement -button
  :tag :button
  :style {:margin-right "1em"
          :padding-top ".2em"
          :padding-bottom ".2em"})

(defn backup-buttons [ctx]
  (let [flags (sub> ctx ::drf/flags)]
    [-content-group
     [-content-area
      [:p "We're only showing off the restore behavior tree, so the backup button is disabled !"]
      [-button {:disabled true}
       "Backup to a file..."]
      [-button {:on-click #(btc/<cmd-send-event ctx :restore-pressed)
                :disabled (not (:restore-button flags))}
       "Restore a saved file..."]]]))

(defn confirm-dialog [ctx]
  (let [flags (sub> ctx ::drf/flags)]
    (when (:confirm-dialog flags) 
      [-content-area
       [panel/render
        {:class :warning}
        "Please confirm."
        [:<> 
         [:div
          "You will lose all the changes that were made since the backup file was saved."]
         [:br]
         [:div
          [-button {:on-click #(open-file-dialog ctx)}
           "I understand, proceed..."]
          [-button {:on-click #(btc/<cmd-send-event ctx :cancel-pressed)}
           "Cancel"]]]]]))  )

(defn restoring-dialog [ctx]
  (let [flags (sub> ctx ::drf/flags)]
    (when (:restoring-dialog flags)      
      [-content-area
       [panel/render
        {:class :info}
        [:div
         (spinner/render)
         "Please wait."]
        [:div 
         [:div            
          "Your database is currently being restored. Please wait."]]]])) )

(defn success-dialog [ctx type]
  (let [flags (sub> ctx ::drf/flags)]
    (when (:success-dialog flags)      
      [-content-area
       [panel/render
        {:class :success}
        [:div "Success !"]
        [:<>
         [:div "The operation completed successfully, maybe the next one will fail !"]
         [:br]
         [-button {:on-click #(btc/<cmd-send-event ctx :ok-pressed)}
          "Ok"]]]])))

(defn error-dialog [ctx type]
  (let [flags (sub> ctx ::drf/flags)]
    (when (:error-dialog flags)      
      [-content-area
       [panel/render
        {:class :danger}
        [:div "Error !"]
        [:<>
         [:div "The operation failed miserably maybe the next one will succeed !" ]
         [:br]
         [-button {:on-click #(btc/<cmd-send-event ctx :ok-pressed)}
          "Ok"]]]])))

(defn render [ctx]
  [page/render
   {:content [:div
              [:h3 "Database backup"]
              [backup-buttons ctx]
              [confirm-dialog ctx]
              [restoring-dialog ctx]
              [success-dialog ctx]
              [error-dialog ctx]]}])

(def component 
  (ui/constructor
   {:renderer render
    :subscription-deps [::drf/flags]
    :component-deps []
    :topic :database-restore})) 


