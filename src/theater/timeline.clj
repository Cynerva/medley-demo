; NOTE: this file is a horrible mess and needs to be cleaned up

(ns theater.timeline
  (:require [quil.core :as q]
            [theater.visual :refer [Visual
                                    update-visual
                                    draw-visual]]
            [theater.transitions :refer [Transition
                                         draw-transition
                                         deftransition]]))

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

(deftransition identity-transition [weight a b]
  (draw-visual a))

(defn make-timeline [& args]
  (loop [events (partition 2 args) visuals [] transitions []]
    (if (seq events)
      (let [event (first events)]
        (if (->> event second (satisfies? Transition))
          (recur (rest events)
                 visuals
                 (conj transitions event))
          (recur (rest events)
               (conj visuals event)
               (conj transitions [(first event) identity-transition]))))
      (Timeline. 0 visuals transitions))))
