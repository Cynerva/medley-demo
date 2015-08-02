(ns theater.audio
  (:require [clojure.java.shell :refer [sh]])
  (:import [java.io File]
           [javax.sound.sampled AudioSystem]))

(defn ffmpeg [& args]
  (apply sh "ffmpeg" args))

(defn load-stream [path]
  (AudioSystem/getAudioInputStream (File. path)))

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
