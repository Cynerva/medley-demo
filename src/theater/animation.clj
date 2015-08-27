(ns theater.animation
  (:require [theater.visual :refer [Visual
                                    update-visual
                                    draw-visual]]))

(defmulti draw-transition (fn [transition-type weight a b]
                            transition-type))

(defmethod draw-transition nil [_ weight a b]
  (draw-visual a))

(defmethod draw-transition :fade [_ weight a b]
  (draw-visual a)
  (draw-visual b))

(defrecord Timeline [age visuals transitions]
  Visual
  (update-visual [this delta]
    (Timeline. (+ age delta)
               (mapv #(vector (first %)
                              (update-visual (second %)
                                             delta))
                     visuals)
               transitions))
  (draw-visual [this]
    (apply draw-transition
           (->> transitions
                (take-while #(>= age (first %)))
                last
                second)
           0
           (loop [previous [0 #()]
                  current (first visuals)
                  remainder (rest visuals)]
             (if (or (nil? current) (> (first current) age))
               (mapv second [previous current])
               (recur current
                      (first remainder)
                      (rest remainder)))))))

(defn make-timeline [& args]
  (Timeline. 0
             (partition 2 args)
             [[0 nil]
              [2 :fade]
              [4 nil]]))
