(ns theater.core
  (:require [clojure.java.shell :refer [sh]]
            [quil.core :as q]
            [theater.audio :refer [load-audio
                                   get-audio-frames
                                   with-audio-playing]]
            [theater.visuals :as visuals]))

(def frame-rate 30)
(def resolution [1366 768])
(def sketch-draw-fn (atom #()))
(def start-time (atom nil))
(def start-frame (atom nil))
(def finished (atom (promise)))

(defn sketch-setup []
  (q/smooth)
  (q/frame-rate frame-rate))

(q/defsketch master-sketch
  :title "theater"
  :size resolution
  :renderer :p2d
  :setup sketch-setup
  :draw #(@sketch-draw-fn))

(defn stop-sketch []
  (reset! sketch-draw-fn #())
  (deliver @finished nil))

(defn start-sketch [draw-fn]
  (stop-sketch)
  (reset! sketch-draw-fn (fn []
                           (reset! start-time (q/millis))
                           (reset! start-frame (q/frame-count))
                           (reset! sketch-draw-fn draw-fn)
                           (draw-fn)))
  (reset! finished (promise)))

(defn sketch [draw-fn]
  (start-sketch draw-fn)
  @@finished)

(defn get-sketch-time []
  (/ (- (q/millis) @start-time) 1000))

(defn get-sketch-frame []
  (- (q/frame-count) @start-frame))

(defn play-demo [visual audio]
  (future
    (with-audio-playing audio
      (let [visual (atom visual)
            last-time (atom 0)
            duration (:duration audio)]
        (sketch #(let [current-time (get-sketch-time)]
                   (swap! visual visuals/update (- current-time @last-time))
                   (visuals/draw! @visual)
                   (reset! last-time current-time)
                   (if (> current-time duration)
                     (stop-sketch))))))))

(defn stop-demo []
  (stop-sketch))

(defn clean-render-folder []
  (sh "rm" "-rf" "/tmp/theater-render"))

(defn render-video [video-path audio-path]
  (println "Final render started")
  (println (sh "ffmpeg"
               "-y"
               "-r" (str frame-rate)
               "-i" video-path
               "-i" audio-path
               "-codec:v" "libx264"
               "-codec:a" "libvorbis"
               "/tmp/test.mp4")))

(defn render-demo [visual audio]
  (future
    (clean-render-folder)
    (let [visual (atom visual)
          end-frame (* (:duration audio) frame-rate)]
      (sketch (fn []
                (swap! visual visuals/update (/ 1 frame-rate))
                (visuals/draw! @visual)
                (q/save (str "/tmp/theater-render/" (get-sketch-frame) ".png"))
                (if (> (get-sketch-frame) end-frame)
                  (stop-sketch)))))
    (render-video "/tmp/theater-render/%d.png" (:path audio))))

(let [audio (load-audio "/home/ava/music/fmtrk2/select.ogg")]
  (play-demo [(fn []
                (q/no-stroke)
                (q/fill 255 255 255 64)
                (q/rect 0 0 (q/width) (q/height)))
              (visuals/make-fog [128 0 255])
              (visuals/make-scope [0 0 0] (get-audio-frames audio))]
             audio))

;(stop-demo)
