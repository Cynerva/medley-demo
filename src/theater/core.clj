(ns theater.core
  (:require [clojure.java.shell :refer [sh]]
            [quil.core :as q]
            [theater.audio :as audio]))

(defn setup []
  (q/smooth)
  (q/frame-rate 60))

(defn draw-scene [scene]
  (q/background 0)
  (q/stroke 255)
  (q/stroke-weight 2)
  (let [frames (take (q/width) scene)]
    (doseq [[x [frame next-frame]] (->> (map vector frames (rest frames))
                                        (map-indexed vector))]
      (q/line x
              (* (first frame) (q/height))
              (inc x)
              (* (first next-frame) (q/height))))))

(defn update-scene [scene delta]
  (drop (* 44100 delta) scene))

(defn sketch-live [initial-scene]
  (let [scene (atom initial-scene) last-time (atom 0)]
    (q/sketch
     :setup setup
     :draw #(do
              (swap! scene update-scene (/ (- (q/millis) @last-time) 1000))
              (reset! last-time (q/millis))
              (draw-scene @scene))
     :size [1366 768])))

(defn clean-render-folder! []
  (sh "rm" "-rf" "/tmp/theater-render"))

(defn sketch-render [initial-scene duration]
  (clean-render-folder!)
  (let [scene (atom initial-scene)]
    (q/sketch
     :setup setup
     :draw #(do
              (swap! scene update-scene (/ 1 60))
              (draw-scene @scene)
              (q/save-frame "/tmp/theater-render/########.png")
              (if (> (q/frame-count) (* duration 60))
                (q/exit)))
     :size [1366 768])))

(sketch-render (audio/load-frames "/tmp/select.wav") 60)
(sh "ffmpeg" "-r" "60" "-i" "/tmp/theater-render/%8d.png" "-i" "/tmp/select.wav" "-codec:v" "libx264" "-codec:a" "libvorbis" "/tmp/test.mp4")
