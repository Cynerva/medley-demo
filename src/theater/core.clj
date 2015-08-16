(ns theater.core
  (:require [clojure.java.shell :refer [sh]]
            [quil.core :as q]
            [theater.audio :refer [load-audio-info]]
            [theater.visuals :as visuals]))

(defn setup []
  (q/smooth)
  (q/frame-rate 15))

(defn play-demo [visual]
  (let [visual (atom visual)
        timestamp (atom 0)]
    (q/sketch
     :setup setup
     :draw #(do
              (swap! visual visuals/update (/ (- (q/millis) @timestamp) 1000))
              (reset! timestamp (q/millis))
              (visuals/draw! @visual))
     :size [1366 768])))

(defn clean-render-folder! []
  (sh "rm" "-rf" "/tmp/theater-render"))

(defn render-demo [audio visual]
  (clean-render-folder!)
  (let [visual (atom visual)]
    (q/sketch
     :setup setup
     :draw #(do (swap! visual visuals/update (/ 1 15))
                (visuals/draw! @visual)
                (q/save-frame "/tmp/theater-render/########.png")
                (if (> (q/frame-count) (* (:duration audio) 15))
                  (q/exit)))
     :size [1366 768])))

(let [audio (load-audio-info "/tmp/select.wav")]
  (render-demo audio [#(q/background 0)
              #(q/stroke 255)
              (visuals/new-scope (:frames audio))]))

; TODO: add this to render-demo
;(sh "ffmpeg" "-r" "60" "-i" "/tmp/theater-render/%8d.png" "-i" "/tmp/select.wav" "-codec:v" "libx264" "-codec:a" "libvorbis" "/tmp/test2.mp4")
