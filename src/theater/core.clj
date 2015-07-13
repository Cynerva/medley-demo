(ns theater.core
  (require [quil.core :as q]))

(defn setup []
  (q/smooth)
  (q/frame-rate 60)
  (q/background 128))

(defn clear []
  (q/stroke-weight 0)
  (q/fill 0)
  (q/rect 0 0 (q/width) (q/height)))

(defn draw []
  (q/stroke (q/random 255) (q/random 255) (q/random 255))
  (q/stroke-weight (q/random 10))
  (q/fill 0)
  (doseq [i (range 10)]
    (let [d (q/random 100)]
      (q/ellipse (q/random (q/width))
                 (q/random (q/height))
                 d d))))

(q/defsketch example
  :title "Oh so many grey circles"
  :setup setup
  :draw draw
  :size [1366 768])
