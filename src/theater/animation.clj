; NOTE: this file is a horrible mess and needs to be cleaned up

(ns theater.animation
  (:require [quil.core :as q]
            [theater.visual :refer [Visual
                                    update-visual
                                    draw-visual]]
            [theater.transitions :refer [draw-transition]]))

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
