(ns cark.keechma.behavior-tree.example.system
  (:require
   [cark.keechma.behavior-tree.example.ui.main :as ui.main]
   [cark.keechma.behavior-tree.example.ui.header :as ui.header]
   [cark.keechma.behavior-tree.example.ui.traffic-lights-page :as ui.traffic-lights]
   [cark.keechma.behavior-tree.example.ui.database-restore-page :as ui.db-restore]))

(defn system []
  {:main ui.main/component
   :header ui.header/component
   :traffic-lights-page ui.traffic-lights/component
   :database-restore-page ui.db-restore/component}) 

