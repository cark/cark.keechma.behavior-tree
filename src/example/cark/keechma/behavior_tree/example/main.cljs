(ns ^:dev/always cark.keechma.behavior-tree.example.main
  (:require [keechma.toolbox.css.app :as css]
            [keechma.toolbox.css.core :refer [update-page-css]]
            [cark.keechma.behavior-tree.example.system :as system]
            [cark.keechma.behavior-tree.example.stylesheets.core :refer [stylesheet]]
            [keechma.app-state :as app-state]
            [keechma.toolbox.dataloader.app :as dl.app]
            [cark.keechma.behavior-tree.example.controllers :as controllers]
            [cark.keechma.behavior-tree.example.subscriptions :as subs]
            [cark.keechma.behavior-tree.example.features.traffic-lights :as tlf]
            [cark.keechma.behavior-tree.example.features.database-restore :as drf]))

(defonce running-app (atom nil))

(def routes
  [["" {:page "traffic-lights"}] 
   ":page"]) 

(defn app-definition []
  (-> {:routes routes 
       :controllers (controllers/controllers) 
       :subscriptions (subs/subscriptions)
       :html-element (.getElementById js/document "app") 
       :components (system/system)}  
      (dl.app/install {} {}) 
      (css/install (stylesheet))
      (tlf/install)
      (drf/install)))

(defn start-app! [old-app-db] 
  (reset! running-app (app-state/start! (if old-app-db
                                          (assoc (app-definition)
                                                 :initial-data (select-keys [:kv :entity-db] @old-app-db))
                                          (app-definition)) true))
  (update-page-css (stylesheet))) 

(defn restart-app! []
  (let [current @running-app] 
    (if current
      (let [old-app-db (-> @running-app :app-db)]
        (app-state/stop! current #(start-app! old-app-db))) 
      (start-app! nil))))

(defn ^:export start [] ;;^:dev/after-load
  (restart-app!)) 

(restart-app!) 


