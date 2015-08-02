(ns theater.core
  (:require [quil.core :as q]
            [theater.audio :as audio]))

(def frames (atom (audio/load-frames "/tmp/select.wav")))

(defn setup []
  (q/smooth)
  (q/frame-rate 15)
  (q/background 0))

(defn draw []
  (q/fill 0)
  (q/stroke-weight 0)
  (q/rect 0 0 (q/width) (q/height))
  (q/stroke 255)
  (q/stroke-weight 2)
  (let [frames (take (q/width) @frames)]
    (doseq [[x [frame next-frame]] (->> (map vector frames (rest frames))
                                        (map-indexed vector))]
      (q/line x
              (* (first frame) (q/height))
              (inc x)
              (* (first next-frame) (q/height)))))
  (swap! frames #(drop (/ 44100 15) %)))

(q/sketch
 :setup setup
 :draw draw
 :size [1366 768])
