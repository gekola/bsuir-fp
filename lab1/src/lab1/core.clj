(ns lab1.core
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.math.numeric-tower :as math]
            [clojure.string :as string]
            [clojure.inspector :as insp])
  (:gen-class :main true))

(defn eucl-dist [a b]
  (math/sqrt (reduce + (map #(math/expt (- (float %1)
                                           (float %2)) 2)
                            a b))))

(defn hamm-dist [a b]
  (reduce + (map #(if (= %1 %2) 0 1) a b)))

(defn prep-point [x]
  (map #(Double/parseDouble %) (drop-last x)))

(defn ps [data alpha dist-fn]
  (map (fn [d]
         (let [len (reduce +
                          (map #(Math/exp (- (* alpha
                                                (dist-fn (prep-point %)
                                                         (prep-point d)))))
                               data))]
           (list len d)))
       data))

(defn clasterize [data dist-fn]
  (let [thr-above 0.5
        thr-below 0.15
        ra 3 ;; Change me
        rb (* ra 1.5)
        alpha (/ 4 (math/expt ra 2))
        beta (/ 4 (math/expt rb 2))]
    (let [fdata (ps data alpha dist-fn)]
      (loop [fdata fdata
             cs ()]
        (let [p1 (or (first (first cs)) 0)
              pxk (reduce (fn [x y] (if (> (first x) (first y))
                                      x y)) fdata)
              pk (first pxk)
              xk (last pxk)
              fdata-n (map
                       #(list
                         (- (first %)
                            (* pk
                               (Math/exp
                                (- (* beta
                                      (dist-fn (prep-point (last %))
                                               (prep-point xk)))))))
                         (last %))
                       fdata)]
          (cond
           (> pk (* thr-above p1)) (recur fdata-n (conj cs pxk))
           (< pk (* thr-below p1)) cs
           :else (let [dmin (reduce #(if (> %1 %2) %2 %1)
                                    (map #(dist-fn (prep-point xk)
                                                   (prep-point (last %))) cs))]
                   (if (>= (+ (/ dmin ra) (/ pk p1)) 1)
                     (recur fdata-n (conj cs pxk))
                     (recur (map #(if (= xk (last %)) (list 0 xk) %) fdata)
                            cs)))))))))

(defn run [file dist]
  (with-open [in-file (io/reader file)]
    (let [data (filter #(and (not-empty %) (not-empty (first %)))
                       (csv/read-csv in-file))]
      (println (string/join "\n" (cons "Clasters:"
                                       (map str (clasterize data dist))))))))

(defn -main
  "The application's main function"
  ([file] (run file eucl-dist))
  ([file dist] (run file (case dist
                           "eucl" eucl-dist
                           "hamm" hamm-dist))))
