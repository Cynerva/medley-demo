(ns theater.core
  (:require [clojure.java.shell :refer [sh]]
            [quil.core :as q]
            [theater.audio :as audio]
            [theater.visuals :as visuals]))

(def frame-rate 15)
(def resolution [1366 768])

(defn setup []
  (q/smooth)
  (q/frame-rate frame-rate))

(defn sketch [draw-fn]
  (let [finished (promise)]
    (q/sketch
     :setup setup
     :draw draw-fn
     :size resolution
     :on-close #(deliver finished nil))
    @finished))

(defn play-demo [visual audio-info]
  (let [visual (atom visual)
        last-time (atom 0)
        duration (:duration audio-info)]
    (sketch #(let [current-time (/ (q/millis) 1000)]
               (swap! visual visuals/update (- current-time @last-time))
               (visuals/draw! @visual)
               (reset! last-time current-time)
               (if (> current-time duration)
                 (q/exit))))))

(defn clean-render-folder []
  (sh "rm" "-rf" "/tmp/theater-render"))

(defn render-video [video-path audio-path]
  (sh "ffmpeg" "-y"
      "-r" (str frame-rate)
      "-i" video-path
      "-i" audio-path
      "-codec:v" "libx264"
      "-codec:a" "libvorbis"
      "/tmp/test.mp4"))

(defn render-demo [visual audio-info]
  (clean-render-folder)
  (let [visual (atom visual)
        end-frame (* (:duration audio-info) frame-rate)]
    (sketch #(do (swap! visual visuals/update (/ 1 frame-rate))
                 (visuals/draw! @visual)
                 (q/save-frame "/tmp/theater-render/########.png")
                 (if (> (q/frame-count) end-frame)
                   (q/exit)))))
  (render-video "/tmp/theater-render/%8d.png" (:path audio-info)))

(let [audio-info (audio/load-info "/tmp/select.wav")]
  (render-demo [#(q/background 0)
              #(q/stroke 255)
              (visuals/make-scope (audio/load-frames (:path audio-info)))]
             audio-info))
