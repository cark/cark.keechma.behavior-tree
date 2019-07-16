(ns cark.keechma.behavior-tree.example.subscriptions
  (:require [keechma.toolbox.dataloader.subscriptions :as dataloader])
  (:require-macros [reagent.ratom :as ratom :refer [reaction]]))

(defn initialized? [app-db-atom]
  (reaction
   (get-in @app-db-atom (flatten [:kv :initialized?]))))

(defn subscriptions []
  {:initialized? initialized?})
