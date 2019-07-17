(ns cark.keechma.behavior-tree.controller
  "Use the namespace provided here to create your own keechma behavior tree controllers"
  (:require [cark.behavior-tree.core :as bt]
            [cark.behavior-tree.context :as ctx]
            [cark.behavior-tree.db :as db]
            [cark.behavior-tree.tree :as tree]
            [keechma.controller :as controller]
            [cljs.core.async :as s :refer [<!]]
            [keechma.toolbox.ui :refer [<cmd]]))

(defn- log [value]
  (js/console.log value)
  value)

(defrecord Controller [controller-api bt-ctx bt-init-func bt-events])

(defn name->app-db-path
  "Returns the path to the behavior tree database in the app-db"
  [name]
  [:kv name])

(defn app-db-path
  "Returns the path to the behavior tree database in the app-db, as seen by the controller."
  [controller]
  (name->app-db-path (:name controller)))

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

(defn handle-bt-event
  "This internal function handles an event coming out of the behavior tree."
  [^Controller this app-db-atom event-name arg]
  (let [handler (get (:bt-events this) event-name)]
    (when handler
      (handler this app-db-atom arg))))

(defn handle-bt-events
  "This internal function loops over the events coming out of the behavior tree,
processing these according to the controller's configuration."
  [^Controller this app-db-atom]
  (let [events (bt/get-events (get-in @app-db-atom (app-db-path this)))]
    (doseq [[event-name arg] events]
      (handle-bt-event this app-db-atom event-name arg))))

(defmethod controller/handler Controller [^Controller this app-db-atom in-chan out-chan]
  (let [bt-ctx (:bt-ctx this)
        app-db-path (app-db-path this)
        bt-init (:bt-init-func this)
        with-bt (fn [app-db func]
                  (update-in app-db app-db-path
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

(defn execute-tick
  "This function ticks the behavior tree. 
It may be called during the execution of the :on-start and :on-stop function as well as
during the execution of the behavior tree event handlers."
  [^Controller controller]
  (controller/execute controller ::tick))

(defn execute-send-event
  "This function sends a behavior tree event to the controller's behavior tree.
It may be called during the execution of the :on-start and :on-stop functions as well as
during the execution of the behavior tree event handlers."
  ([^Controller controller event-name]
   (execute-send-event controller event-name nil))
  ([^Controller controller event-name event-arg]
   (controller/execute controller ::send-event [event-name event-arg])))

(defn <cmd-tick
  "This function sends a command to the controller. 
This will result in the controller's behavior tree to be ticked. That function is intended to be used from a component,
passing its context as parameter."
  [ctx]
  (<cmd ctx ::tick))

(defn <cmd-send-event
  "This function send a command to the controller. 
This will result in sending a behavior tree event to the controller's behavior tree.
The function is intended to be used from a component, passing its context as the first parameter.
The second parameter is the event name, a keyword.
The third optional parameter is the event argument."
  ([ctx event-name]
   (<cmd ctx ::send-event [event-name nil]))
  ([ctx event-name arg]
   (<cmd ctx ::send-event [event-name arg])))

(def default-api
  {:on-start (fn [^Controller this params app-db] app-db)
   :on-stop (fn [^Controller this params app-db] app-db)
   :params (constantly nil)
   :tree-init-func identity})

(defn make
  "Returns a new behavior tree controller, customized with a behavior tree and associated parameters.
Parameters:
- params-fun : The params function is a regular keechma controller params function. It receives the controller as first parameter, 
and a route-params map as second parameter. It should return true if the controller is to be run in that context, false otherwise.
- controller-api : This map may have several keys :
  - :on-start : this is the optional regular :on-start keeshma controller function. It receives the controller as first parameter,
the params map as second argument, and the app-db as third argument. This function must return a possibly updated app-db.
  - :on-stop : this is the optional regular :on-stop keeshma controller function. It receives the controller as first parameter,
the params map as second argument, and the app-db as third argument. This function must return a possibly updated app-db.
  - :tree : this is the actual behavior tree context, as returned by bt/hiccup->context on an hiccup notation behavior tree.
  - :tree-init-func : this function receives the current behavior tree context and returns an initialized tree. This function is
called when the controller is started
  - :tree-events : this map has keyword event names as keys and event handler functions as values. An event handler receives
the controller instance as its first parameter, the app-db-atom as its second parameter and the event arg as third parameter."
  [params-func controller-api]
  (let [api (merge (assoc default-api :params params-func)
                   controller-api)]
    (->Controller api
                  (:tree api) 
                  (:tree-init-func api)
                  (:tree-events api))))

(defn get-blackboard
  "Returns the blackboard for this controller"
  [app-db controller-name]  
  (bt/bb-get (get-in app-db (name->app-db-path controller-name))))

(defn update-blackboard
  "Updates the blackboard of this controller with the func and args."
  [app-db-atom controller-name func & args]
  (apply swap! app-db-atom update-in (name->app-db-path controller-name) bt/bb-update func args))

(defn set-blackboard
  "Sets the balckboard value for this controller."
  [app-db-atom controller-name value]
  (update-blackboard app-db-atom controller-name (constantly value)))
