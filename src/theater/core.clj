(ns theater.core
  (:require [quil.core :as q]
            [theater.demo :refer [make-demo
                                  play-demo
                                  stop-demo
                                  render-demo]]
            [theater.audio :refer [load-audio
                                   get-audio-frames]]
            [theater.visual :refer [make-scope
                                    make-fog]]
            [theater.timeline :refer [make-timeline]]
            [theater.transitions :refer [fade
                                         fade-blank]]))

(defn make-test-demo []
  (let [audio (load-audio "/home/ava/music/fmtrk2/select.ogg")]
    (make-demo audio [#(q/background 32 0 64)
                      (make-timeline
                        0 (make-scope [255 255 255]
                                      (get-audio-frames audio)
                                      (:frame-rate audio))
                        5 fade
                        10 (make-fog {:color [0 255 0 128]
                                     :count 10}))
                      (make-timeline
                       0 #()
                       1 fade
                       2 #(q/text "Text example" 100 100)
                       5 fade-blank
                       7 #(q/text "DUDE JESSIE" 1000 600)
                       9 fade-blank
                       11 #(doseq [x (range 0 (q/width) 200) y (range 0 (q/height) 50)]
                             (q/text "BREAKFAST CORNDOGS" x y))
                       19 fade
                       27 #())])))

(play-demo (make-test-demo))

(stop-demo)
