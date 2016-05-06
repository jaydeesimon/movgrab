(ns movgrab.pdf
  (:require [clojure.java.io :as io])
  (:use clj-pdf.core)
  (:import (javax.imageio ImageIO)
           (java.io File)))

(comment

  (def image (ImageIO/read (io/file (io/resource "lenna100.jpg"))))
  (def f0 (ImageIO/read (io/file (io/resource "222.png"))))
  (def files [(io/file (io/resource "lenna100.jpg")) (io/file (io/resource "222.png"))])
  (pdf
    [{}
     [:image f0]
     [:pagebreak]
     [:image image]]
    "test.pdf"))

(defn- file->img-tag [^File f]
  [:image (.getAbsolutePath f)])

(defn gen-pdf [img-files out]
  (let [pdf-settings {}
        img-tags (map file->img-tag img-files)]
    (pdf (concat [pdf-settings]
                 (interpose [:pagebreak] img-tags))
         out)))