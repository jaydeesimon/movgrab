(ns movgrab.core
  (:require [clojure.java.io :as io]
            [movgrab.pdf :as pdf]
            [movgrab.math :refer [std-deviation]])
  (:gen-class)
  (:import (org.bytedeco.javacv FFmpegFrameGrabber Java2DFrameConverter)
           (javax.imageio ImageIO)
           (java.awt.image BufferedImage)
           (java.io File)))

(defmacro with-started-grabber [binding & body]
  (let [[grabber-symb ^File mov] binding]
    `(let [~grabber-symb (doto (FFmpegFrameGrabber. mov) (.start))]
       (try
         ~@body
         (finally
           (.stop ~grabber-symb))))))

;; I based this on https://rosettacode.org/wiki/Percentage_difference_between_images
;; so there's plenty wrong with it but it seems to do the job for now
;; Also, I'm only comparing 50% of random pixels instead of all of them
(defn diff-ratio [^BufferedImage img-a ^BufferedImage img-b]
  {:pre [(and (= (.getWidth img-a) (.getWidth img-b))
              (= (.getHeight img-a) (.getHeight img-b)))]}
  (let [width (.getWidth img-a)
        height (.getHeight img-a)
        pixel-coords (for [x (range width)
                           y (range height)]
                       [x y])
        pixel-coords (take (int (/ (* (count pixel-coords)) 2)) (shuffle pixel-coords))
        diffs (map (fn [[x y]]
                     (Math/abs (- (.getRGB img-a x y) (.getRGB img-b x y)))) pixel-coords)]
    (/ (reduce + diffs) (* width height 0xFFFFFF))))

(defn temp-file [prefix suffix]
  (doto (File/createTempFile prefix suffix) (.deleteOnExit)))

(defn mov->frames [^FFmpegFrameGrabber grabber]
  (let [frames (range 1 (.getLengthInFrames grabber) 120)
        frame-converter (Java2DFrameConverter.)]
    (map-indexed (fn [i frame-n]
           (do
             (let [^File file (temp-file (format "%06d" i) ".png")]
               (.setFrameNumber grabber frame-n)
               (println "Set Frame to" frame-n)
               (ImageIO/write (.convert frame-converter (.grabImage grabber)) "png" file)
               file)))
         frames)))

(defn frame-diff-ratios [frame-files]
  (map (fn [[^File file-a ^File file-b]]
         (let [^BufferedImage bia (ImageIO/read file-a)
               ^BufferedImage bib (ImageIO/read file-b)]
           {:file-a file-a :file-b file-b :diff-ratio (diff-ratio bia bib)}))
       (partition 2 1 frame-files)))

(defn detect-frame-changes [frame-ratios]
  (let [std-dev (std-deviation (map :diff-ratio frame-ratios))]
    (->> frame-ratios
         (filter #(> (:diff-ratio %) std-dev)))))

(defn slides [frame-changes]
  (concat [(:file-a (first frame-changes))]
          (map :file-b frame-changes)))

;; lein run <some-movie-file> <some-output.pdf>
(defn -main [& args]
  (with-started-grabber [grabber (io/file (first args))]
    (let [frame-files (mov->frames grabber)]
      (-> frame-files
          (frame-diff-ratios)
          (detect-frame-changes)
          (slides)
          (pdf/gen-pdf (second args))))))
