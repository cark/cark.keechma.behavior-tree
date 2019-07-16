(ns cark.keechma.behavior-tree.example.controllers
  (:require [cark.keechma.behavior-tree.example.controllers.initializer :as initializer]))

(defn controllers []
  (-> {}
      initializer/register))
