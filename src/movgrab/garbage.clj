(ns movgrab.garbage)

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

(comment

  (def mov (io/file (io/resource "rich_hickey_design.mov")))

  ;; Current usage
  (def grabber (new-grabber mov))

  (def slides (slides (detected-frame-changes (frame-ratios (mov->frames grabber)))))

  (pdf/gen-pdf slides "test.pdf")

  ;; Need to stop the grabber
  (.stop grabber))
