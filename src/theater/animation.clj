(ns theater.animation
  (:require [quil.core :as q]
            [theater.visual :refer [Visual
                                    update-visual
                                    draw-visual]]))

(defmacro with-alpha [alpha & body]
  `(let [graphics# (q/create-graphics (q/width) (q/height))]
    (q/with-graphics graphics#
      (q/background 0 0 0 0)
      ~@body)
    (q/tint 255 255 255 ~alpha)
    (q/image graphics# 0 0)))

(defmulti draw-transition (fn [transition-type weight a b]
                            transition-type))

(defmethod draw-transition nil [_ weight a b]
  (draw-visual a))

(defmethod draw-transition :fade [_ weight a b]
  (with-alpha (* 255 (- 1 weight))
    (draw-visual a))
  (with-alpha (* 255 weight)
    (draw-visual b)))

(defn get-transition-weight [age transitions]
  (loop [previous [0 nil]
         current (first transitions)
         remainder (rest transitions)]
    (if (nil? current)
      0
      (if (> (first current) age)
        (let [start (first previous)
              end (first current)]
          (/ (- age start)
             (- end start)))
        (recur current
               (first remainder)
               (rest remainder))))))

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
           (get-transition-weight age transitions)
           (loop [previous [0 #()]
                  current (first visuals)
                  remainder (rest visuals)]
             (if (or (nil? current) (> (first current) age))
               (mapv second [previous current])
               (recur current
                      (first remainder)
                      (rest remainder)))))))

(defn make-timeline [& args]
  (loop [events (partition 2 args) visuals [] transitions []]
    (if (seq events)
      (let [event (first events)]
        (if (-> event second keyword?)
          (recur (rest events)
                 visuals
                 (conj transitions event))
          (recur (rest events)
               (conj visuals event)
               (conj transitions [(first event) nil]))))
      (Timeline. 0 visuals transitions))))
