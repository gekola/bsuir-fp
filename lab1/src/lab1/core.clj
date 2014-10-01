(ns lab1.core
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.math.numeric-tower :as math]
            [clojure.string :as string])
  (:gen-class :main true))

(defn eucl-dist [a b]
  (math/sqrt (reduce + (map #(math/expt (- (float %1)
                                           (float %2)) 2)
                            a b))))

(defn hamm-dist [a b]
  (reduce + (map #(if (= %1 %2) 0 1) a b)))

(defn ps [data alpha dist-fn]
  (map (fn [d]
         (let [len (reduce +
                           (map #(Math/exp (- (* alpha
                                                 (dist-fn % d))))
                                data))]
           (list len d)))
       data))

(defn prep-point [x]
  (map #(Double/parseDouble %) (drop-last x)))

(defn clusterize [data p-dist-fn]
  (let [thr-above 0.5
        thr-below 0.15
        ra 3 ;; Change me
        rb (* ra 1.5)
        alpha (/ 4 (math/expt ra 2))
        beta (/ 4 (math/expt rb 2))
        dist-fn (fn [x y] (apply p-dist-fn (map prep-point (list x y))))
        fdata (ps data alpha dist-fn)]
    (loop [fdata fdata
           cs ()]
      (let [p1 (or (first (first cs)) 0)
            pxk (apply max-key first fdata)
            pk (first pxk)
            xk (last pxk)
            fdata-n (map
                     #(cons
                       (- (first %)
                          (* pk
                             (Math/exp
                              (- (* beta (dist-fn (last %) xk))))))
                       (rest %))
                     fdata)]
        (cond
         (> pk (* thr-above p1)) (recur fdata-n (conj cs pxk))
         (< pk (* thr-below p1)) cs
         :else (let [dmin (apply min
                                 (map #(dist-fn xk (last %)) cs))]
                 (if (>= (+ (/ dmin ra) (/ pk p1)) 1)
                   (recur fdata-n (conj cs pxk))
                   (recur (map #(if (= xk (last %)) (list 0 xk) %) fdata)
                          cs))))))))

(defn run [file dist]
  (with-open [in-file (io/reader file)]
    (let [data (filter #(and (not-empty %) (not-empty (first %)))
                       (csv/read-csv in-file))]
      (println (string/join "\n"
                            (cons "Clusters:"
                                  (map str (clusterize data dist))))))))

(defn -main
  "The application's main function"
  ([file] (run file eucl-dist))
  ([file dist] (run file (case dist
                           "eucl" eucl-dist
                           "hamm" hamm-dist))))
