(ns lab2.core
  (:require [org.httpkit.client :as http])
  (:gen-class))

(defn url-without-ref [url]
  (first (clojure.string/split url #"#")))

(defn find-links [body url]
  (distinct
   (filter #(and (not (= url %))
                 (not (nil? %))
                 (< 0 (count %)))
           (map (comp url-without-ref second)
                (re-seq #"(?i)<a[^>]+href=\"([^\"]+)\"[^>]*>"
                        (.toString body))))))

(defn node-for [url, {:keys [status headers body error] :as resp}]
  (if error
    [[url 0 "bad" []] []]
    (if (= status 200)
      (let [links (find-links body url)
            cnt (count links)]
        [[url cnt (str cnt) []] links])
    ;else
      [[url 0 "redir" []] []])))

(defn handle-req [[par-url par-sz par-msg par-children] url depth]
  (let [resp @(http/get url)
        [node urls] (node-for url resp)]
    ;; (if (> 0 depth)
    [par-url par-sz par-msg (cons (first (node-for url resp)) par-children)]))


(defn populate-node [par-node urls depth]
  (reduce (fn [node url] (handle-req node url depth)) par-node urls))

(defn crawl [depth url]
  (let [resp @(http/get url)
        [node urls] (node-for url resp)]
    (let [tree (populate-node node urls (dec depth))]
      (do
        (println tree)
        (doseq [n (nth tree 3)]
          (println (first n) (nth n 2)))))))

(defn -main [& args]
  (case (count args)
    2 (apply crawl (cons (int (first args)) (rest args)))
    1 (apply crawl (cons 3 args))
    (println "Format: lein run [depth] url")))
