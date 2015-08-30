(ns theater.transitions
  (:require [quil.core :as q]
            [theater.visual :refer [draw-visual]]))

(defprotocol Transition
  (draw-transition [this weight a b]))

(defmacro make-transition [args & body]
  `(reify Transition
     (draw-transition [this# ~@args]
        ~@body)))

(defmacro deftransition [name args & body]
  `(def ~name (make-transition ~args ~@body)))

(defmacro with-tint [color & body]
  `(let [graphics# (q/create-graphics (q/width) (q/height))]
    (q/with-graphics graphics#
      (q/no-smooth) ; FIXME: no-smooth should be abstracted out of with-tint
      (q/background 0 0 0 0)
      ~@body)
    (q/tint ~@color)
    (q/image graphics# 0 0)))

(defmacro with-alpha [alpha & body]
  `(with-tint [255 255 255 ~alpha] ~@body))

(deftransition fade [weight a b]
  (with-alpha (* 255 (- 1 weight))
    (draw-visual a))
  (with-alpha (* 255 weight)
    (draw-visual b)))

(defn make-fade-through [intermediate]
  (make-transition [weight a b]
    (if (< weight 0.5)
      (draw-transition fade
                       (* weight 2)
                       a
                       intermediate)
      (draw-transition fade
                       (dec (* weight 2))
                       intermediate
                       b))))

(def fade-blank (make-fade-through #()))

(def fade-white (make-fade-through #(q/background 255 255 255)))
