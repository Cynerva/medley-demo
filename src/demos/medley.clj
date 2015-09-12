(ns demos.medley
  (:require [quil.core :as q]
            [theater.demo :refer [make-demo
                                  play-demo
                                  stop-demo
                                  render-demo]]
            [theater.audio :refer [load-audio
                                   get-audio-frames]]
            [theater.visual :refer [Visual
                                    make-scope
                                    make-fog]]
            [theater.timeline :refer [make-timeline]]
            [theater.transitions :refer [fade
                                         fade-blank]]))

(def titles
  [0 "animus"
   128/3 "boxytron 5"
   256/3 "crystal science"
   384/3 "definite disorder"
   512/3 "esther's wasteland"
   640/3 "friendly flabebe"
   784/3 "gummy bears on the run"
   880/3 "here, together, we are strong"
   976/3 "intermission"
   1072/3 "john freeman"
   1216/3 "k"
   1408/3 "lucky ducky"
   1504/3 "monty sucks"
   1600/3 "no really, he sucks"
   1696/3 "ok stop"])

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
  (q/fill 192 0 192)
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
     (q/stroke 192 0 192)
     (q/stroke-weight 4)
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

(defn draw-background-ellipse [t r]
  (q/ellipse (/ (q/width) 2)
             (/ (q/height) 2)
             (* (q/cos (* t (q/random 0.5))) r)
             (* (q/sin (* t (q/random 0.5))) r)))

(defrecord BackgroundOsc [t]
  Visual
  (update-visual [this delta]
    (assoc this :t (+ t delta)))
  (draw-visual [this]
    (q/random-seed 32490)
    (q/color-mode :hsb)
    (q/stroke (mod (- (* 255/3 2) t) 255) 255 255)
    (q/color-mode :rgb)
    (q/stroke-weight 2)
    (q/no-fill)
    (doseq [i (range 2 12 0.5)]
      (draw-background-ellipse t (Math/pow 2 i)))))

(defn make-background-osc []
  (BackgroundOsc. 0))

(defn make-medley-visual [audio]
  [(make-timeline
    0 (fn []
        (q/fill 0 0 0 255)
        (q/no-stroke)
        (q/rect 0 0 (q/width) (q/height)))
    0 fade
    64/3 [(fn []
           (q/no-stroke)
           (q/fill 0 0 0 64)
           (q/rect 0 0 (q/width) (q/height)))
          (make-background-osc)])
   (make-console audio)])

(defn make-medley-demo []
  (let [audio (load-audio "/home/ava/lmms/projects/Medley/Medley.flac")]
    (make-demo audio (make-medley-visual audio))))

; Repl stuffs

(defn refresh []
  (require ['demos.medley :reload true]))

(defn play []
  (play-demo (make-medley-demo)))

(defn render []
  (render-demo (make-medley-demo)))

(def stop stop-demo)
