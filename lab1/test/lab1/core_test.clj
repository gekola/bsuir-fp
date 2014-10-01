(ns lab1.core-test
  (:require [clojure.test :refer :all]
            [lab1.core :refer :all]))

(deftest hamm-dist-test
  (testing
      (is (= 1 (hamm-dist [1 2 3] [1 2 2])))
      (is (= 0 (hamm-dist [1 1] [1 1])))
      (is (= 5 (hamm-dist [1 2 3 4 5] [2 3 4 5 1])))))

(deftest eucl-dist-test
  (testing
      (is (= 1.0 (eucl-dist [1 2 3] [1 2 2])))
      (is (= 0.0 (eucl-dist [1 1] [1 1])))
      (is (= 5.0 (eucl-dist [3 4] [0 0])))))
