(ns cark.keechma.behavior-tree.example.trees.restore-test
  (:require [clojure.test :as t :refer [deftest is]]
            [cark.behavior-tree.core :as bt]
            [cark.behavior-tree.context :as ctx]
            [cark.keechma.behavior-tree.example.features.database-restore :as r]))

(deftest restore-test
  (let [ctx (-> r/ctx)
        send-event (fn send-event
                     ([ctx event]
                      (send-event ctx event nil))
                     ([ctx event arg]
                      (-> (bt/send-event ctx event arg) bt/tick)))
        send-events (fn send-events [ctx & events]
                      (reduce #(apply send-event %1 %2)
                              ctx (map #(if (seqable? %) % [%]) events)))]
    (is ctx)
    
    (is (-> ctx (bt/bb-get-in [:restore-button])))
    
    (is (= :running (-> (send-events ctx :restore-pressed)
                        bt/get-status)))
    
    (is (not (-> (send-events ctx :restore-pressed)
                 (bt/bb-get-in [:restore-button])))) 
    
    (is (-> (send-events ctx :restore-pressed)
            (bt/bb-get-in [:confirm-dialog])))
    
    (is (not (-> (send-events ctx :restore-pressed :cancel-pressed)
                 (bt/bb-get-in [:confirm-dialog]))))
    
    (is (-> (send-events ctx :restore-pressed :cancel-pressed)
            (bt/bb-get-in [:restore-button])))
    
    (is (not (-> (send-events ctx :restore-pressed :cancel-pressed :restore-pressed)
                 (bt/bb-get-in [:restore-button]))))
    
    (is (-> (send-events ctx :restore-pressed :cancel-pressed :restore-pressed)
            (bt/bb-get-in [:confirm-dialog])))

    (let [ctx (send-events ctx :restore-pressed [:got-file-data :some-data])]
      ;; this new context has received file data
      
      (is (= [[:restore-file :some-data]]
             (-> ctx bt/get-events)))
      
      (is (not (-> ctx (bt/bb-get-in [:confirm-dialog]))))
      
      (is (-> ctx (bt/bb-get-in [:restoring-dialog])))
      
      (is (not (-> (send-events ctx [:restore-result :success])
                   (bt/bb-get-in [:restoring-dialog]))))
      
      (is (-> (send-events ctx [:restore-result :success])
              (bt/bb-get-in [:success-dialog])))
      
      (is (not (-> (send-events ctx [:restore-result :success])
                   (bt/bb-get-in [:error-dialog]))))
      
      (is (-> (send-events ctx [:restore-result :success])
              (bt/bb-get-in [:restore-button])))

      (is (not (-> (send-events ctx [:restore-result :error])
                   (bt/bb-get-in [:restoring-dialog]))))
      
      (is (-> (send-events ctx [:restore-result :error])
              (bt/bb-get-in [:error-dialog])))
      
      (is (not (-> (send-events ctx [:restore-result :error])
                   (bt/bb-get-in [:success-dialog]))))
      
      (is (-> (send-events ctx [:restore-result :success])
              (bt/bb-get-in [:restore-button])))

      (is (not (-> (send-events ctx [:restore-result :success] :ok-pressed)
                   (bt/bb-get-in [:success-dialog]))))
      
      (is (not (-> (send-events ctx [:restore-result :error] :ok-pressed)
                   (bt/bb-get-in [:error-dialog]))))

      (is (not (-> (send-events ctx [:restore-result :error] :restore-pressed)
                   (bt/bb-get-in [:restore-button]))))
      
      (is (-> (send-events ctx [:restore-result :error] :restore-pressed)
              (bt/bb-get-in [:confirm-dialog])))

      (is (not (-> (send-events ctx [:restore-result :error] :restore-pressed)
                   (bt/bb-get-in [:error-dialog])))))))
