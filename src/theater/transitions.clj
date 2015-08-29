(ns theater.transitions
  (:require [quil.core :as q]
            [theater.visual :refer [draw-visual]]))

(defmulti draw-transition (fn [transition-type weight a b]
                            transition-type))

(defmethod draw-transition nil [_ weight a b]
  (draw-visual a))

(defmacro with-tint [color & body]
  `(let [graphics# (q/create-graphics (q/width) (q/height))]
    (q/with-graphics graphics#
      (q/background 0 0 0 0)
      ~@body)
    (q/tint ~@color)
    (q/image graphics# 0 0)))

(defmacro with-alpha [alpha & body]
  `(with-tint [255 255 255 ~alpha] ~@body))

(defmethod draw-transition :fade [_ weight a b]
  (with-alpha (* 255 (- 1 weight))
    (draw-visual a))
  (with-alpha (* 255 weight)
    (draw-visual b)))
