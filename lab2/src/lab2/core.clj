(ns lab2.core
  (:require [org.httpkit.client :as http]
            [com.climate.claypoole :as cp])
  (:gen-class))

(def pool (cp/threadpool 20))

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
    [[url 0 "bad" nil] []]
    (if (= status 200)
      (if (re-matches #".*html.*" (:content-type headers))
        (let [links (find-links body url)
              cnt (count links)]
          [[url cnt (str cnt) (agent [])] links])
      ;else
        [[url 0 0 nil] []])
    ;else
      [[url 0 "redir" nil] []])))

(defn handle-req [[par-url _ _ par-children :as par-node] url depth]
  (if par-children
    (let [resp @(http/get url {:follow-redirects false})
          [node urls] (node-for url resp)
                                        ; [par-url par-sz par-msg (cons (first (node-for url resp)) par-children)]
          [child-node links] (node-for url resp)]
      (do
        (send par-children (partial cons child-node))
        (if (< 0 depth)
          (cp/pfor pool [url urls]
                         (handle-req child-node url (dec depth))))))))

(defn populate-node [par-node urls depth]
  (reduce (fn [node url]
            (handle-req node url depth))
          par-node
          urls))

(defn print-node [[par-url par-cnt par-str par-children] prefix]
  (let [x (promise)]
    (println prefix par-url par-str)
    (if par-children
      (add-watch par-children
                 :key
                 (fn [key _ _ children]
                   (if (= par-cnt (count children))
                     (do
                       (doseq [n children]
                         (print-node n (clojure.string/join [prefix "  "])))
                       (deliver x nil))))))
    @x))

(defn crawl [depth url]
  (let [resp @(http/get url)
        [node urls] (node-for url resp)]
    ;(let [tree (populate-node node urls (dec depth))]
    (do
      (cp/pfor pool [url urls]
                     (handle-req node url (dec depth)))
      (print-node node ""))))

(defn -main [& args]
  (do
    (case (count args)
      2 (apply crawl (cons (Integer/parseInt (first args)) (rest args)))
      1 (apply crawl (cons 3 args))
      (println "Format: lein run [depth] url"))
    (cp/shutdown pool)
    (shutdown-agents)))
