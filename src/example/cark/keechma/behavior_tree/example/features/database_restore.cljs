(ns cark.keechma.behavior-tree.example.features.database-restore
  (:require [keechma.controller :as controller]
            [cark.keechma.behavior-tree.controller :as btc]
            [cark.behavior-tree.core :as bt]
            [cark.behavior-tree.state-machine :as sm]
            [reagent.ratom :refer [reaction]]
            [clojure.set :as set]))

(defn log [value]
  (js/console.log value)
  value)

;; Behavior tree

;; we define a backup restore page
;;
;; - a button "restore" will start the process
;; - first a confirmation dialog will open
;; - restore button is grayed out
;; - the dialog cancel button makes the confirm dialog disapear
;; - the dialog's confirm button will start the open file dialog
;;   browers do not allow to do this progralmatically, so we'll leave this to the ui
;; - html5 only gives a result when the user selects a file, not when he
;; cancels the open file dialog,we need to work around this
;; - when we have the file data, we close the confirmation dialog and send a restore event
;; - we open a "restoring" dialog
;; - we receive a result that might be
;; - success : open a success dialog having an ok button, activate restore button
;; - failure : open a failure dialog having an ok button, activate restore button

(def ctx
  (-> (sm/make [:sm] :start 
        (sm/state :start
          (sm/enter-event [:update {:func (bt/bb-updater-in [:flags] set/union #{:restore-button})}])
          (sm/event :restore-pressed (sm/transition :restore))
          (sm/event :ok-pressed
            [:update {:func (bt/bb-updater-in [:flags] set/difference #{:success-dialog
                                                                        :error-dialog})}]))
        (sm/state :restore
          (sm/enter-event [:update {:func (bt/bb-assocer-in [:flags] #{:confirm-dialog})}])
          (sm/event :cancel-pressed
            [:sequence
             [:update {:func (bt/bb-assocer-in [:flags] #{})}]
             (sm/transition :start)])
          (sm/event :got-file-data
            [:sequence
             [:send-event {:event :restore-file :arg sm/event-arg}]
             [:update {:func (bt/bb-assocer-in [:flags] #{:restoring-dialog})}]])
          (sm/event :restore-result
            [:sequence
             [:update {:func
                       #(cond-> (bt/bb-update % assoc :flags #{:restore-button})
                          (= :success (first (sm/event-arg %))) (bt/bb-update-in [:flags] conj :success-dialog)
                          (= :error (first (sm/event-arg %))) (-> (bt/bb-update-in [:flags] conj :error-dialog)
                                                                  (bt/bb-update assoc :error (second (sm/event-arg %)))))}]
             (sm/transition :start)])))
      bt/hiccup->context bt/tick))

;; controller

(def restore-result (atom (cycle [:success :error])))

(defn restore-file [controller app-db-atom arg]
  (js/setTimeout #(do (btc/execute-send-event controller :restore-result [(first @restore-result) nil])
                      (swap! restore-result rest))
                 1500))

(defn controller []
  (btc/make (fn [controller route-params]
              (= "database-restore" (get-in route-params [:data :page]))) 
            {:tree ctx
             :tree-init-func (constantly ctx)
             :tree-events {:restore-file restore-file}}))

(defn subscriptions []
  (let [blackboard (btc/blackboard-reaction :database-restore)
        flags (fn [app-db-atom]
                (let [bb (blackboard app-db-atom)]
                  (reaction (:flags @bb))))]
    {::flags flags}))

(defn install [app-def]
  (-> app-def
      (update :subscriptions merge (subscriptions) )
      (update :controllers assoc :database-restore (controller))))





