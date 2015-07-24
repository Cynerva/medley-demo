(ns theater.audio
  (:require [clojure.java.shell :refer [sh]])
  (:import [java.io File]
           [javax.sound.sampled AudioSystem]))

(defn ffmpeg [& args]
  (apply sh "ffmpeg" args))

(defn read-audio-file [path]
  (AudioSystem/getAudioInputStream (File. path)))
