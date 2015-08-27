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
            [theater.animation :refer [make-timeline]]))

(defn make-test-demo []
  (let [audio (load-audio "/home/ava/music/fmtrk2/select.ogg")]
    (make-demo audio [(fn []
                        (q/background 32 0 64))
                      (make-timeline
                        0 (make-scope [255 255 255]
                                      (get-audio-frames audio)
                                      (:frame-rate audio))
                        4 (make-fog {:color [0 255 0 128]
                                     :count 10}))])))

(play-demo (make-test-demo))

(stop-demo)
