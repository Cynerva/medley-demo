(ns theater.visuals
  (:require [quil.core :as q]))

(defprotocol Visual
  (update [this delta])
  (draw! [this]))

(extend-type clojure.lang.Seqable
  Visual
  (update [this delta]
    (map #(update % delta) this))
  (draw! [this]
    (doseq [visual this]
      (draw! visual))))

(extend-type clojure.lang.IFn
  Visual
  (update [this delta]
    this)
  (draw! [this]
    (this)))

(defrecord Scope [color audio-frames]
  Visual
  (update [this delta]
    (Scope. color (drop (* 44100 delta) audio-frames)))
  (draw! [this]
    (apply q/stroke color)
    (q/stroke-weight 2)
    (let [frames (take (q/width) audio-frames)]
      (doseq [[x [frame next-frame]] (->> (map vector frames (rest frames))
                                          (map-indexed vector))]
        (q/line x
                (* (first frame) (q/height))
                (inc x)
                (* (first next-frame) (q/height)))))))

(defn make-scope [color audio-frames]
  (Scope. color audio-frames))

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
  (update [this delta]
    (Fog. color
          (if (seq circles)
            (map #(update-fog-circle % delta) circles)
            (take 100 (repeatedly make-random-fog-circle-with-random-age)))))
  (draw! [this]
    (doseq [circle circles]
      (draw-fog-circle circle color))))

(defn make-fog [color]
  (Fog. color nil))
