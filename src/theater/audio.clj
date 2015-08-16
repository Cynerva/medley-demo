(ns theater.audio
  (:require [clojure.java.shell :refer [sh]])
  (:import [java.io File]
           [javax.sound.sampled AudioSystem]))

(defn load-stream [path]
  (let [file (File/createTempFile "theater" ".wav")]
    (sh "ffmpeg" "-i" path (.getAbsolutePath file) "-y")
    (AudioSystem/getAudioInputStream file)))

(defn bytes->uint [bytes]
  (loop [bytes (reverse bytes) sum 0]
    (if (seq bytes)
      (recur (rest bytes)
             (+ (* sum 256)
                (+ (first bytes) 128)))
      sum)))

(defn bytes->float [bytes]
  (/ (bytes->uint bytes)
     (Math/pow 256 (count bytes))))

(defn parse-frame [sample-size bytes]
  (map bytes->float
       (partition sample-size bytes)))

(defn get-sample-size [stream]
  (/ (-> stream .getFormat .getSampleSizeInBits) 8))

(defn read-next-frame! [stream]
  (let [bytes (byte-array (-> stream .getFormat .getFrameSize))]
    (.read stream bytes)
    (parse-frame (get-sample-size stream) bytes)))

(defn read-frames! [stream]
  (repeatedly (.getFrameLength stream)
              #(read-next-frame! stream)))

(defn load-frames [path]
  (read-frames! (load-stream path)))

(defn read-audio-info [stream]
  (let [frame-rate (-> stream .getFormat .getFrameRate)]
    {:duration (/ (.getFrameLength stream) frame-rate)
     :frame-rate frame-rate
     :frames (read-frames! stream)}))

(defn load-audio-info [path]
  (assoc (read-audio-info (load-stream path))
    :path path))
