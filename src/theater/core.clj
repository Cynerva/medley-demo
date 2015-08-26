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
            [theater.animation :refer [make-animation]]))

(defn make-test-demo []
  (let [audio (load-audio "/home/ava/music/fmtrk2/select.ogg")]
    (make-demo audio [(fn []
                        (q/background 32 0 64))
                      (make-animation 2
                                      (make-scope [255 255 255]
                                                  (get-audio-frames audio)
                                                  (:frame-rate audio))
                                      (make-fog {:color [0 255 0 32]
                                                 :count 100}))])))

(play-demo (make-test-demo))

(stop-demo)
