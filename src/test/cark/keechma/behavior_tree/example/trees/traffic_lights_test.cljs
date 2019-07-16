(ns cark.keechma.behavior-tree.example.trees.traffic-lights-test
  (:require [clojure.test :as t :refer [deftest is]]
            [cark.behavior-tree.core :as bt]
            [cark.behavior-tree.context :as ctx]
            [cark.keechma.behavior-tree.example.features.traffic-lights :as tl]))

(deftest crossroad-test
  (let [report (comp (juxt :we :ns) bt/bb-get) 
        crossroad (-> tl/crossroad (bt/tick 0) (bt/tick+ 5000))]
    (is (= [:red :green] (-> crossroad report))) 
    (is (= [:red :yellow] (-> crossroad (bt/tick+ 3000) report)))
    (is (= [:red :red] (-> crossroad (bt/tick+ 3000) (bt/tick+ 1000) report)))
    (is (= [:green :red] (-> crossroad (bt/tick+ 3000) (bt/tick+ 1000) (bt/tick+ 1000) report)))
    (is (= [:yellow :red] (-> crossroad (bt/tick+ 3000) (bt/tick+ 1000) (bt/tick+ 1000) (bt/tick+ 3000) report)))
    (is (= [:red :red] (-> crossroad (bt/tick+ 3000) (bt/tick+ 1000) (bt/tick+ 1000) (bt/tick+ 3000) (bt/tick+ 1000) report)))
    (is (= [:red :green] (-> crossroad (bt/tick+ 3000) (bt/tick+ 1000) (bt/tick+ 1000) (bt/tick+ 3000) (bt/tick+ 1000) (bt/tick+ 1000) report)))))

