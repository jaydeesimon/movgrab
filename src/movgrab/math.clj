(ns movgrab.math)

(defn mean [xs]
  (/ (reduce + xs) (count xs)))

(defn variance [xs]
  (let [mean' (mean xs)]
    (mean (map #(Math/pow (- % mean') 2) xs))))

(defn std-deviation [xs]
  (Math/sqrt (variance xs)))
