(ns cark.keechma.behavior-tree.controller
  "USe the namespace provided here to create your own keechma nehavior tree controllers"
  (:require [cark.behavior-tree.core :as bt]
            [cark.behavior-tree.context :as ctx]
            [cark.behavior-tree.db :as db]
            [cark.behavior-tree.tree :as tree]
            [keechma.controller :as controller]
            [cljs.core.async :as s :refer [<!]]
            [keechma.toolbox.ui :refer [<cmd]]))

(defn log [value]
  (js/console.log value)
  value)

(defrecord Controller [controller-api bt-ctx bt-init-func bt-events])

(defn name->app-db-path
  "Returns the path to the database "
  [name]
  [:kv name])

(defn app-db-path [controller]
  (name->app-db-path (:name controller)))

(defn name->bt-db-path [name]
  (conj (name->app-db-path name) :bt-db))

(defn bt-db-path [controller]
  (name->bt-db-path (:name controller)))

(defmethod controller/params Controller [^Controller this route-params]
  (let [params-func (get-in this [:controller-api :params])]
    (params-func this route-params)))

(defmethod controller/start Controller [^Controller this params app-db]
  (let [start-func (get-in this [:controller-api :on-start])]
    (controller/execute this ::start params)
    (start-func this params app-db)))

(defmethod controller/stop Controller [^Controller this params app-db]
  (let [stop-func (get-in this [:controller-api :on-stop])]
    (controller/execute this ::stop params)
    (stop-func this params app-db)))

(defn handle-bt-event [^Controller this app-db-atom event-name arg]
  (let [handler (get (:bt-events this) event-name)]
    (when handler
      (handler this app-db-atom arg))))

(defn handle-bt-events [^Controller this app-db-atom]
  (let [events (bt/get-events (get-in @app-db-atom (bt-db-path this)))]
    (doseq [[event-name arg] events]
      (handle-bt-event this app-db-atom event-name arg))))

(defmethod controller/handler Controller [^Controller this app-db-atom in-chan out-chan]
  (let [bt-ctx (:bt-ctx this)
        app-db-path (app-db-path this)
        bt-db-path (bt-db-path this)
        bt-init (:bt-init-func this)
        with-bt (fn [app-db func]
                  (update-in app-db bt-db-path
                             (fn [bt-db]
                               (-> (if bt-db
                                     (bt/merge-db bt-ctx bt-db)
                                     bt-ctx)
                                   func
                                   bt/extract-db))))]    
    (s/go-loop []
      (let [[command arg] (<! in-chan)]
        (when command
          (try
            (case command
              ::start (do (swap! app-db-atom with-bt bt-init)
                          (handle-bt-events this app-db-atom))
              ::stop (do (swap! app-db-atom with-bt 
                                #(let [root-id (tree/get-root-node-id %)]
                                   (ctx/set-node-status % root-id :fresh)))
                         (handle-bt-events this app-db-atom))
              ::tick (do (swap! app-db-atom with-bt bt/tick)
                         (handle-bt-events this app-db-atom))
              ::send-event (do (swap! app-db-atom with-bt
                                      #(-> (apply bt/send-event % arg)
                                           bt/tick))
                               (handle-bt-events this app-db-atom))
              nil)
            (catch js/Object ex
              (js/console.error "BT handler error" {:command command
                                                    :arg arg
                                                    :error ex})))
          (recur))))))

(defn execute-tick [^Controller controller]
  (controller/execute controller ::tick))

(defn execute-send-event
  ([^Controller controller event-name]
   (execute-send-event controller event-name nil))
  ([^Controller controller event-name event-arg]
   (controller/execute controller ::send-event [event-name event-arg])))

(defn <cmd-tick [ctx]
  (<cmd ctx ::tick))

(defn <cmd-send-event
  ([ctx event-name]
   (<cmd ctx ::send-event [event-name nil]))
  ([ctx event-name arg]
   (<cmd ctx ::send-event [event-name arg])))

(def default-api
  {:on-start (fn [^Controller this params app-db] app-db)
   :on-stop (fn [^Controller this params app-db] app-db)
   :params (constantly nil)
   :tree-init-func identity})

(defn make [params-func controller-api]
  (let [api (merge (assoc default-api :params params-func)
                   controller-api)]
    (->Controller api
                  (:tree api) 
                  (:tree-init-func api)
                  (:tree-events api))))

(defn get-blackboard [app-db controller-name]
  (bt/bb-get (get-in app-db (name->bt-db-path controller-name))))

(defn update-blackboard [app-db-atom controller-name func & args]
  (apply swap! app-db-atom update-in (name->bt-db-path controller-name) bt/bb-update func args))

(defn set-blackboard [app-db-atom controller-name value]
  (update-blackboard app-db-atom controller-name (constantly value)))
