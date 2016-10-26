(ns game.glitch
 (require [arcadia.core :as a]
          [arcadia.linear :as l])
 (import [UnityEngine CameraClearFlags Camera]))

(defn set-depth-clearing [depth]
 (let [camera-left (a/cmpt (a/object-named "Camera Left") Camera)
       camera-right (a/cmpt (a/object-named "Camera Right") Camera)
       depth (if (= depth :skybox) CameraClearFlags/Skybox CameraClearFlags/Nothing)]
  (doseq [camera [camera-left camera-right]]
   (set! (.clearFlags camera) depth))))
   