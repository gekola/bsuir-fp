(ns lab2.core-test
  (:require [clojure.test :refer :all]
            [lab2.core :refer :all]))

(deftest parsing
  (testing "Links parsing"
    (testing "recognizes html <a> tag"
      (is (= (find-links "<a href=\"http://example.com/\">" "")
             ["http://example.com/"])))
    (testing "removes anchors from url"
      (is (= (find-links "<a href=\"http://example.com/#asdf\">" "")
             ["http://example.com/"])))
    (testing "filters base url from results"
      (is (= (find-links "<a href=\"http://example.com/#asdf\">"
                         "http://example.com/")
             [])))))

(deftest error-handling
  (with-redefs [org.httpkit.client/get (fn [& args]
                           (future {:status 404, :headers {}, :body "", :error "Not found"}))]
    (testing "404 handling"
      (is (= (url-to-node-with-urls "http://example.com/" false) [["http://example.com/" 0 "bad" nil] []])))))
