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

(defrecord Scope [audio-frames]
  Visual
  (update [this delta]
    (Scope. (drop (* 44100 delta) audio-frames)))
  (draw! [this]
    (let [frames (take (q/width) audio-frames)]
      (doseq [[x [frame next-frame]] (->> (map vector frames (rest frames))
                                          (map-indexed vector))]
        (q/line x
                (* (first frame) (q/height))
                (inc x)
                (* (first next-frame) (q/height)))))))

(defn make-scope [audio-frames]
  (Scope. audio-frames))
