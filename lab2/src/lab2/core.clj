(ns lab2.core
  (:use clojurewerkz.urly.core)
  (:refer-clojure :exclude [resolve])
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [org.httpkit.client :as http]
            [com.climate.claypoole :as cp])
  (:gen-class))

(def pool (cp/threadpool 50))

(defn url-without-ref [url]
  (first (string/split url #"#")))

(defn complete-link [base url]
  (if url
    (if (absolute? url)
      url
      (if base (resolve base url)))))

(defn find-links [body url]
  (distinct
   (filter #(and (not (= url %))
                 (not (nil? %))
                 (< 0 (count %)))
           (map (comp (partial complete-link url)
                      url-without-ref
                      second)
                (re-seq #"(?i)<a[^>]+href=\"([^\"]+)\"[^>]*>"
                        (.toString body))))))

(defn node-for [url {:keys [status headers body error] :as resp} last]
  (if error
    [[url 0 "bad" nil] []]
    (if (= status 200)
      (if (re-matches #".*html.*" (:content-type headers))
        (let [links (find-links body url)
              cnt (count links)]
          (if last
            [[url 0 (str cnt) nil] []]
          ;else
            [[url cnt (str cnt) (agent [])] links]))
      ;else
        [[url 0 "0" nil] []])
    ;else
      [[url 0 "redir" nil] []])))

(defn url-to-node-with-urls [url skip-children]
  (let [resp @(http/get url {:follow-redirects false})]
    (node-for url resp skip-children)))

(defn handle-req [[par-url _ _ par-children] url depth]
  (if par-children
    (let [[child-node urls] (url-to-node-with-urls url (<= depth 0))]
      (send par-children (partial cons child-node))
      (cp/pfor pool [url urls]
               (handle-req child-node url (- depth 1))))))

(defn print-node [[par-url par-cnt par-str par-children] prefix]
  (let [ready-children (promise)]
    (println (string/join [prefix par-url " " par-str]))
    (if par-children
      (do
        (add-watch par-children
                   :key
                   (fn [_ _ _ children]
                     (println [(count children) "of" par-cnt])
                     (if (= par-cnt (count children))
                       (deliver ready-children children))
                     true))
        (let [children (deref par-children)]
          (if (= par-cnt (count children))
            (deliver ready-children children))))
    ;else
      (deliver ready-children []))
    (doseq [c @ready-children]
      (print-node c (string/join [prefix "  "])))))

(defn crawl [depth file]
  (with-open [in-file (io/reader file)]
    (let [urls (line-seq in-file)
          nodes (cp/upmap pool
                      (fn [url]
                        (let [[node urls] (url-to-node-with-urls url (<= depth 0))]
                          (cp/pfor pool [url urls]
                                   (handle-req node url (- depth 1)))
                          node))
                      urls)]
      (doseq [node nodes]
        (print-node node "")))))

(defn -main [& args]
  (case (count args)
    2 (apply crawl (cons (Integer/parseInt (first args)) (rest args)))
    1 (apply crawl (cons 3 args))
    (println "Format: lein run [depth] url"))
  (cp/shutdown pool)
  (shutdown-agents))
