(ns theater.demo
  (:require [clojure.java.shell :refer [sh]]
            [quil.core :as q]
            [theater.audio :refer [with-audio-playing]]
            [theater.visual :refer [update-visual
                                    draw-visual]]))

(def frame-rate 30)
(def resolution [1366 768])
(def sketch-draw-fn (atom #()))
(def start-time (atom nil))
(def start-frame (atom nil))
(def finished (atom (promise)))

(defn- sketch-setup []
  (q/no-smooth)
  (q/frame-rate frame-rate))

(q/defsketch master-sketch
  :title "theater"
  :size resolution
  :setup sketch-setup
  :draw #(@sketch-draw-fn))

(defn- stop-sketch []
  (reset! sketch-draw-fn #())
  (deliver @finished nil))

(defn- start-sketch [draw-fn]
  (stop-sketch)
  (reset! sketch-draw-fn (fn []
                           (reset! start-time (q/millis))
                           (reset! start-frame (q/frame-count))
                           (reset! sketch-draw-fn draw-fn)
                           (draw-fn)))
  (reset! finished (promise)))

(defn- sketch [draw-fn]
  (start-sketch draw-fn)
  @@finished)

(defn- get-sketch-time []
  (/ (- (q/millis) @start-time) 1000))

(defn- get-sketch-frame []
  (- (q/frame-count) @start-frame))

(defn- clean-render-folder []
  (sh "rm" "-rf" "/tmp/theater-render"))

(defn- render-video [video-path audio-path]
  (println "Final render started")
  (println (sh "ffmpeg"
               "-y"
               "-r" (str frame-rate)
               "-i" video-path
               "-i" audio-path
               "-codec:v" "libx264"
               "-codec:a" "libvorbis"
               "/tmp/test.mp4")))

(defn play-demo [visual audio]
  (future
    (with-audio-playing audio
      (let [visual (atom visual)
            last-time (atom 0)
            duration (:duration audio)]
        (sketch #(let [current-time (get-sketch-time)]
                   (swap! visual update-visual (- current-time @last-time))
                   (draw-visual @visual)
                   (reset! last-time current-time)
                   (if (> current-time duration)
                     (stop-sketch))))))))

(defn stop-demo []
  (stop-sketch))

(defn render-demo [visual audio]
  (future
    (clean-render-folder)
    (let [visual (atom visual)
          end-frame (* (:duration audio) frame-rate)]
      (sketch (fn []
                (swap! visual update-visual (/ 1 frame-rate))
                (draw-visual @visual)
                (q/save (str "/tmp/theater-render/" (get-sketch-frame) ".png"))
                (if (> (get-sketch-frame) end-frame)
                  (stop-sketch)))))
    (render-video "/tmp/theater-render/%d.png" (:path audio))))
