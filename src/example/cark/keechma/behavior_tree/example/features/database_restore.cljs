(ns cark.keechma.behavior-tree.example.features.database-restore
  (:require [keechma.controller :as controller]
            [cark.keechma.behavior-tree.controller :as btc]
            [cark.behavior-tree.core :as bt]
            [reagent.ratom :refer [reaction]]))

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
  (-> [:sequence
       ;;init
       [:update {:func (bt/bb-updater assoc :flags #{:restore-button})}]
       ;;keep running
       [:parallel {:policy {:success :every :failure :every} :rerun-children true}
        ;; the start point, checking the restore button
        [:guard [:predicate {:func (bt/bb-getter-in [:flags :restore-button])}]
         [:on-event {:event :restore-pressed :wait? true}
          [:update {:func (bt/bb-updater assoc :flags #{:confirm-dialog})}]]]
        ;; confirm dialog
        [:guard [:predicate {:func (bt/bb-getter-in [:flags :confirm-dialog])}]
         [:on-event {:event :cancel-pressed :wait? true}
          [:update {:func (bt/bb-updater assoc :flags #{:restore-button})}]]]
        ;; got file data
        [:on-event {:event :got-file-data :bind-arg :file-data
                    :wait? true}
         [:sequence
          [:update {:func (bt/bb-updater assoc :flags #{:restoring-dialog})}]
          [:send-event {:event :restore-file :arg (bt/var-getter :file-data)}]]]
        ;; got restore operation result
        [:on-event {:event :restore-result :bind-arg :result :wait? true}
         [:update {:func #(cond-> (bt/bb-update % assoc :flags #{:restore-button})
                            (= :success (bt/get-var % :result)) (bt/bb-update update :flags conj :success-dialog) 
                            (= :error (bt/get-var % :result)) (bt/bb-update update :flags conj :error-dialog))}]]
        ;; success dialog
        [:guard [:predicate {:func (bt/bb-getter-in [:flags :success-dialog])}]
         [:on-event {:event :ok-pressed :wait? true}
          [:update {:func (bt/bb-updater update :flags disj :success-dialog)}]]]
        ;; error dialog
        [:guard [:predicate {:func (bt/bb-getter-in [:flags :error-dialog])}]
         [:on-event {:event :ok-pressed :wait? true}
          [:update {:func (bt/bb-updater update :flags disj :error-dialog)}]]]]]
      bt/hiccup->context bt/tick))


;; controller

(def restore-result (atom (cycle [:success :error])))

(defn restore-file [controller app-db-atom arg]
  (js/setTimeout #(do (btc/execute-send-event controller :restore-result (first @restore-result))
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





