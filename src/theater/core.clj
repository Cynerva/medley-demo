(ns theater.core
  (require [quil.core :as q]))

(defn setup []
  (q/smooth)
  (q/frame-rate 60)
  (q/background 128))

(defn fill-screen [& colors]
  (q/stroke-weight 0)
  (apply q/fill colors)
  (q/rect 0 0 (q/width) (q/height)))

(defn draw-random-horizontal-line []
  (let [y (q/random (q/height))]
    (q/line 0 y (q/width) y)))

(defn draw-random-vertical-line []
  (let [x (q/random (q/width))]
    (q/line x 0 x (q/height))))

(defn draw-random-line []
  (if (< (q/random) 0.5)
    (draw-random-horizontal-line)
    (draw-random-vertical-line)))

(defn draw-random-lines [count]
  (doseq [_ (range count)]
    (draw-random-line)))

(defn draw []
  ;(q/random-seed 0)
  ;(fill-screen 0)
  (q/stroke 255)
  (q/stroke-weight 10)
  (draw-random-line))

(q/sketch
 :setup setup
 :draw draw
 :size [1366 768])
