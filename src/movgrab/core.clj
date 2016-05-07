(ns movgrab.core
  (:require [clojure.java.io :as io]
            [movgrab.pdf :as pdf])
  (:gen-class)
  (:import (org.bytedeco.javacv FFmpegFrameGrabber Java2DFrameConverter)
           (javax.imageio ImageIO)
           (java.awt.image BufferedImage)
           (java.io File)))

(def mov (io/file (io/resource "rich_hickey_design.mov")))

(comment
  (FFmpegFrameGrabber. mov)
  (.start grabber)
  (.getLengthInFrames grabber)
  (.setFrameNumber grabber 1)
  (.grabImage grabber)
  (def frame-converter (Java2DFrameConverter.))
  (.stop grabber)

  ;; Image Stuff
  (def lenna50 (ImageIO/read (io/file (io/resource "lenna50.jpg"))))
  (def lenna100 (ImageIO/read (io/file (io/resource "lenna100.jpg"))))
  (def white (ImageIO/read (io/file (io/resource "white.png"))))
  (def black (ImageIO/read (io/file (io/resource "black.png"))))
  (def black1 (ImageIO/read (io/file (io/resource "black-1.png"))))
  (def f0 (ImageIO/read (io/file (io/resource "222.png"))))
  (def f1 (ImageIO/read (io/file (io/resource "223.png"))))
  (.getRGB lenna 0 2))

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

;; Ideally, I want to create a with-grabber macro
(defn new-grabber [^File mov]
  (doto (FFmpegFrameGrabber. mov) (.start)))

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

(defn frame-ratios [frame-files]
  (map (fn [[^File file-a ^File file-b]]
         (let [^BufferedImage bia (ImageIO/read file-a)
               ^BufferedImage bib (ImageIO/read file-b)]
           {:file-a file-a :file-b file-b :diff-ratio (diff-ratio bia bib)}))
       (partition 2 1 frame-files)))

(defn mean [xs]
  (/ (reduce + xs) (count xs)))

(defn variance [xs]
  (let [mean' (mean xs)]
    (mean (map #(Math/pow (- % mean') 2) xs))))

(defn std-deviation [xs]
  (Math/sqrt (variance xs)))

(defn detected-frame-changes [frame-ratios]
  (let [std-dev (std-deviation (map :diff-ratio frame-ratios))]
    (->> frame-ratios
         (filter #(> (:diff-ratio %) std-dev)))))

(defn slides [frame-changes]
  (concat [(:file-a (first frame-changes))]
          (map :file-b frame-changes)))

(comment

  ;; Current usage
  (def grabber (new-grabber mov))

  (def slides (slides (detected-frame-changes (frame-ratios (mov->frames grabber)))))

  (pdf/gen-pdf slides "test.pdf")

  ;; Need to stop the grabber
  (.stop grabber))


