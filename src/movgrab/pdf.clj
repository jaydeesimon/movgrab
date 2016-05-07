(ns movgrab.pdf
  (:require [clojure.java.io :as io])
  (:use clj-pdf.core)
  (:import (java.io File)))

(defn- file->img-tag [^File f]
  [:image (.getAbsolutePath f)])

(defn gen-pdf [img-files out]
  (let [pdf-settings {}
        img-tags (map file->img-tag img-files)]
    (pdf (concat [pdf-settings]
                 (interpose [:pagebreak] img-tags))
         out)))