(ns theater.animation
  (:require [theater.visual :refer [Visual
                                    update-visual
                                    draw-visual]]))

(defrecord Animation [interval frames age]
  Visual
  (update-visual [this delta]
    (let [frames (mapv #(update-visual % delta) frames)
          new-age (+ age delta)]
      (if (> new-age interval)
        (Animation. interval
                    (conj (subvec frames 1)
                          (first frames))
                    (- new-age interval))
        (Animation. interval
                    frames
                    new-age))))
  (draw-visual [this]
    (draw-visual (first frames))))

(defn make-animation [interval & frames]
  (Animation. interval frames 0))
