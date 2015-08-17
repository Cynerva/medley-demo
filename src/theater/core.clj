(ns theater.core
  (:require [clojure.java.shell :refer [sh]]
            [quil.core :as q]
            [theater.audio :refer [load-audio-info]]
            [theater.visuals :as visuals]))

(def frame-rate 15)

(defn setup []
  (q/smooth)
  (q/frame-rate frame-rate))

(defn sketch [draw-fn]
  (q/sketch
   :setup setup
   :draw draw-fn
   :size [1366 768]))

(defn play-demo [visual]
  (let [visual (atom visual)
        timestamp (atom 0)]
    (sketch #(do (swap! visual visuals/update (/ (- (q/millis) @timestamp) 1000))
                 (reset! timestamp (q/millis))
                 (visuals/draw! @visual)))))

(defn clean-render-folder! []
  (sh "rm" "-rf" "/tmp/theater-render"))

(defn make-video [audio-path]
  (sh "ffmpeg"
      "-r" frame-rate
      "-i" "/tmp/theater-render/%8d.png"
      "-i" audio-path
      "-codec:v" "libx264"
      "-codec:a" "libvorbis"
      "/tmp/test.mp4"))

; TODO: call make-video from here
(defn render-demo [audio visual]
  (clean-render-folder!)
  (let [visual (atom visual)]
    (sketch #(do (swap! visual visuals/update (/ 1 frame-rate))
                 (visuals/draw! @visual)
                 (q/save-frame "/tmp/theater-render/########.png")
                 (if (> (q/frame-count) (* (:duration audio) frame-rate))
                   (q/exit))))))

(let [audio (load-audio-info "/tmp/select.wav")]
  (render-demo audio [#(q/background 0)
              #(q/stroke 255)
              (visuals/new-scope (:frames audio))]))
