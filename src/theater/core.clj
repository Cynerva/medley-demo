(ns theater.core
  (:require [quil.core :as q]
            [theater.demo :refer [play-demo
                                  stop-demo
                                  render-demo]]
            [theater.audio :refer [load-audio
                                   get-audio-frames]]
            [theater.visual :refer [make-animation
                                    make-scope
                                    make-fog]]))

(let [audio (load-audio "/home/ava/music/fmtrk2/select.ogg")]
  (play-demo [(fn []
                (q/background 32 0 64))
              (make-animation 2
                              (make-scope [255 255 255]
                                          (get-audio-frames audio)
                                          (:frame-rate audio))
                              (make-fog {:color [0 255 0 32]
                                         :count 100}))]
             audio))

(stop-demo)
