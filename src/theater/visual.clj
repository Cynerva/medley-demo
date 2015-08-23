(ns theater.visual
  (:require [quil.core :as q]))

(defprotocol Visual
  (update-visual [this delta])
  (draw-visual [this]))

(extend-type clojure.lang.Seqable
  Visual
  (update-visual [this delta]
    (map #(update-visual % delta) this))
  (draw-visual [this]
    (doseq [visual this]
      (draw-visual visual))))

(extend-type clojure.lang.IFn
  Visual
  (update-visual [this delta]
    this)
  (draw-visual [this]
    (this)))

(defrecord Scope [color audio-frame-rate audio-frames]
  Visual
  (update-visual [this delta]
                 (Scope. color
                         audio-frame-rate
                         (drop (* audio-frame-rate delta)
                               audio-frames)))
  (draw-visual [this]
    (apply q/stroke color)
    (q/stroke-weight 2)
    (let [frames (take (q/width) audio-frames)]
      (doseq [[x [frame next-frame]] (->> (map vector frames (rest frames))
                                          (map-indexed vector))]
        (q/line x
                (* (first frame) (q/height))
                (inc x)
                (* (first next-frame) (q/height)))))))

(defn make-scope [color audio-frame-rate audio-frames]
  (Scope. color audio-frame-rate audio-frames))

(defn make-random-fog-circle []
  {:radius (q/random 50 100)
   :pos (map q/random [(q/width) (q/height)])
   :vel (take 2 (repeatedly #(q/random -100 100)))
   :lifetime (q/random 1 2)
   :age 0})

(defn make-random-fog-circle-with-random-age []
  (let [circle (make-random-fog-circle)]
    (assoc circle :age (q/random (:lifetime circle)))))

(defn update-fog-circle [circle delta]
  (if (> (:age circle) (:lifetime circle))
    (make-random-fog-circle)
    (assoc circle
      :pos (mapv +
                 (:pos circle)
                 (mapv (partial * delta) (:vel circle)))
      :age (+ (:age circle) delta))))

(defn get-fog-circle-color [circle color]
  (conj (vec color)
        (* 255
           (q/sin (* (/ (:age circle)
                        (:lifetime circle))
                     q/PI)))))

(defn draw-fog-circle [circle color]
  (q/stroke-weight (:radius circle))
  (apply q/stroke (get-fog-circle-color circle color))
  (apply q/point (:pos circle)))

(defrecord Fog [color circles]
  Visual
  (update-visual [this delta]
    (Fog. color (map #(update-fog-circle % delta) circles)))
  (draw-visual [this]
    (doseq [circle circles]
      (draw-fog-circle circle color))))

(defn make-fog [color]
  (reify Visual
    (update-visual [this delta]
      (Fog. color
            (take 100 (repeatedly make-random-fog-circle-with-random-age))))
    (draw-visual [this])))
