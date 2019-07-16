(ns cark.keechma.behavior-tree.example.features.traffic-lights
  (:require [keechma.controller :as controller]
            [cark.keechma.behavior-tree.controller :as btc]
            [cark.behavior-tree.core :as bt]
            [reagent.ratom :refer [reaction]]))

(defn log [value]
  (js/console.log value)
  value)

;; We define a crossroad with 2 traffic light controllers
;; the :ns north-south controller and the :we west-east controller

(def sec 1000)

(defn secs [n]
  (* n sec))

(defn make-traffic-light [name time-offset]
  [:sequence
   [:timer {:timer name :duration (- time-offset)}]
   [:repeat
    [:sequence
     [:update {:func (bt/bb-assocer-in [name] :green)}]
     [:timer {:timer name :duration (secs 3)}]
     [:update {:func (bt/bb-assocer-in [name] :yellow)}]
     [:timer {:timer name :duration (secs 1)}]
     [:update {:func (bt/bb-assocer-in [name] :red)}]
     [:timer {:timer name :duration (secs 6)}]]]])

(def crossroad 
  (-> [:parallel
       (make-traffic-light :we (secs 0)) 
       (make-traffic-light :ns (secs 5))]
      bt/hiccup->context)) 

(defn init [crossroad] 
  (-> crossroad (bt/tick (- (js/Date.now) (secs 5))) bt/tick))

;; done with behavior tree, let's see to the controller

(defn controller []
  (btc/make (fn [controller route-params] true) 
            {:on-start (fn [controller params app-db]
                         (assoc-in app-db (conj (btc/app-db-path controller) :interval-id)
                                   (js/setInterval #(btc/execute-tick controller) 100))) 
             :on-stop (fn [controller params app-db]
                        (js/clearInterval (get-in app-db (conj (btc/app-db-path controller) :interval-id))))
             :tree crossroad
             :tree-init-func init}))

;; subscriptions

(defn subscriptions []
  (let [blackboard (memoize
                    (fn [app-db-atom]
                      (reaction (btc/get-blackboard @app-db-atom :traffic-lights))))]
    {::north-south (fn [app-db-atom]
                     (let [bb (blackboard app-db-atom)]
                       (reaction (:ns @bb))))
     ::west-east (fn [app-db-atom] 
                   (let [bb (blackboard app-db-atom)]
                     (reaction (:we @bb))))}))

;; install the feature

(defn install [app-def]
  (-> app-def
      (update :subscriptions merge (subscriptions) )
      (update :controllers assoc :traffic-lights (controller))))



