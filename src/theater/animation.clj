(ns theater.animation
  (:require [theater.visual :refer [Visual
                                    update-visual
                                    draw-visual]]))

(defrecord Timeline [age visuals]
  Visual
  (update-visual [this delta]
    (Timeline. (+ age delta)
               (mapv #(vector (first %)
                              (update-visual (second %)
                                             delta))
                     visuals)))
  (draw-visual [this]
    (draw-visual (->> visuals
                      (filter #(>= age (first %)))
                      last
                      second))))

(defn make-timeline [& args]
  (Timeline. 0 (partition 2 args)))
