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

(extend-type clojure.lang.IDeref
  Visual
  (update-visual [this delta]
    (update-visual @this delta))
  (draw-visual [this]
    (draw-visual @this)))

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

(defrecord Scope [color audio-frames audio-frame-rate]
  Visual
  (update-visual [this delta]
                 (Scope. color
                         (drop (* audio-frame-rate delta)
                               audio-frames)
                         audio-frame-rate))
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

(defn make-scope [color audio-frames audio-frame-rate]
  (Scope. color audio-frames audio-frame-rate))

(defn make-random-fog-circle []
  {:radius (q/random 0 500)
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
  (conj (subvec color 0 3)
        (* (get color 3)
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

(defn make-fog [opts]
  (delay
   (Fog. (:color opts)
         (take (:count opts)
               (repeatedly make-random-fog-circle-with-random-age)))))
