(ns theater.core
  (:require [quil.core :as q]
            [theater.demo :refer [make-demo
                                  play-demo
                                  stop-demo
                                  render-demo]]
            [theater.audio :refer [load-audio
                                   get-audio-frames]]
            [theater.visual :refer [make-scope
                                    make-fog]]
            [theater.timeline :refer [make-timeline]]
            [theater.transitions :refer [fade-blank]]))

(def titles
  [0 "animus"
   128/3 "boxytron 5"
   256/3 "crystal science"
   384/3 "definite disorder"
   512/3 "esther's wasteland"
   640/3 "friendly flabebe"
   784/3 "gummy bears on the run"
   880/3 "here, together, we are strong"])

(def console-font (delay (q/create-font "DejaVu Sans Mono" 38)))

(def console-height 100)

(defn draw-console-back []
  (q/no-stroke)
  (q/fill 32 0 32)
  (q/rect 0 0 (q/width) console-height)

  (q/stroke 64 0 64)
  (q/stroke-weight 1)
  (q/no-fill)
  (doseq [x (range 0 (q/width) 20)]
    (q/line x 0 x console-height))
  (doseq [y (range 0 console-height 20)]
    (q/line 0 y (q/width) y))

  (q/stroke 96 96 96)
  (q/stroke-weight 2)
  (q/no-fill)
  (let [x (/ (q/width) 2)]
    (q/line x 0 x console-height))
  (q/line 0 console-height (q/width) console-height))

(defn draw-title [title]
  (q/fill 128 0 128)
  (q/text-font @console-font)
  (q/text-align :center :center)
  (q/text title (/ (q/width) 4) 44))

(defn make-console-title-display []
  (apply make-timeline
         (->> titles
              (partition 2)
              (map (fn [[start title]]
                     [(- start 1/3) fade-blank
                      start #(draw-title title)]))
              flatten)))

(defn make-console-scope [audio]
  [(fn []
     (q/stroke 128 0 128)
     (q/stroke-weight 2)
     (q/no-fill)
     (q/push-matrix)
     (q/scale 0.5 (/ console-height (q/height)))
     (q/translate (q/width) 0))
   (make-scope [128 0 128]
               (get-audio-frames audio)
               (:frame-rate audio))
   #(q/pop-matrix)])

(defn make-console [audio]
  [draw-console-back
   (make-console-title-display)
   (make-console-scope audio)])

(defn make-medley-visual [audio]
  [#(q/background 0 0 0)
   (make-fog {:color [128 0 128 32]
              :count 50})
   (make-console audio)])

(defn make-medley-demo []
  (let [audio (load-audio "/home/ava/lmms/projects/Medley/Medley WIP.flac")]
    (make-demo audio (make-medley-visual audio))))

(play-demo (make-medley-demo))

(stop-demo)
