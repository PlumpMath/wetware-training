(ns game.core
 (require [arcadia.core :as a]
          [arcadia.linear :as l]
          [tween.core :refer :all]
          [game.ui :as ui]
          [game.glitch :as glitch])
 (import [UnityEngine Animator AudioSource Resources Quaternion Renderer Color RectTransform Camera Application]))

(defn emote [emotion wait-duration]
 (let [syd-bod (a/object-named "sydneyBody")
       syd (a/object-named "sydney")
       orig-pos (.. syd transform position)
       orig-color (.color (.material (a/cmpt syd-bod Renderer)))]
  (timeline
   [#(do (a/set-state! syd :moving true) false)
    (wait (+ 2 wait-duration))
    #(do (a/set-state! syd :moving false) false)])
  (a/set-state! syd :moving true)
  (cond
   (= emotion :greet)
   (timeline*
    (AND
     (tween {:position (l/v3+ orig-pos (l/v3 0 0.5 0))} syd 1 {:in :pow3 :out :pow3})
     (tween {:material {:color (Color. 0 0 1 1)}} syd-bod 1 {:in :pow3 :out :pow3}))
    (wait wait-duration)
    (AND
     (tween {:position orig-pos} syd 1 {:in :pow3 :out :pow3})
     (tween {:material {:color orig-color}} syd-bod 1 {:in :pow3 :out :pow3})))
   (= emotion :aside)
   (timeline*
    (AND
     (tween {:position (l/v3+ orig-pos (l/v3 0 0.5 0))} syd 1 {:in :pow3 :out :pow3})
     (tween {:local {:position (l/v3 0 0 -1)}} syd-bod 1 {:in :pow3 :out :pow3})
     (tween {:material {:color (Color. 0.8 0.2 0.2 1)}} syd-bod 1 {:in :pow3 :out :pow3}))
    (wait wait-duration)
    (AND
     (tween {:position orig-pos} syd 1 {:in :pow3 :out :pow3})
     (tween {:local {:position (l/v3 0 0 1)}} syd-bod 1 {:in :pow3 :out :pow3})
     (tween {:material {:color orig-color}} syd-bod 1 {:in :pow3 :out :pow3})))
   (= emotion :pantomime)
   (timeline*
    (AND
     (tween {:position (l/v3+ (l/v3 0 1 0) orig-pos)} syd 1 {:in :pow3 :out :pow3})
     (tween {:material {:color (Color. 0.6 0.6 0.2 1)}} syd-bod 1 {:in :pow3 :out :pow3}))
    (wait wait-duration)
    (AND
     (tween {:position orig-pos} syd 1 {:in :pow3 :out :pow3})
     (tween {:material {:color orig-color}} syd-bod 1 {:in :pow3 :out :pow3}))))))
   
(defn sydney-hover []
 (let [syd-bod (a/object-named "sydneyBody")
       syd (a/object-named "sydney")]
  (timeline* :loop
   #(a/state syd :moving)
   (AND
    (NOT #(a/state syd :moving))
    (OR (tween {:local {:position (l/v3 0 0.2 0)}} syd-bod 1)
        (tween {:local {:position (l/v3 0 -0.2 0)}} syd-bod 1))))))

(defn sydney-entrance []
 (let [syd (a/object-named "sydney")]
  (timeline
   [#(do (a/set-state! syd :moving true) false)
     (tween {:position (l/v3 0 6 0)} syd 2 {:in :pow3 :out :pow3})
     (tween {:position (l/v3 0 5 2)} syd 2 {:in :pow2 :out :pow2})
     (tween {:position (l/v3 0 8 2)} syd 1 {:in :pow4 :out :pow4})
     (tween {:position (l/v3 0 2 4)} syd 3 {:in :pow3 :out :pow3})
     #(do (a/set-state! syd :moving false) false)])))

(defn turn-on-sign []
 (let [sign-anim (a/cmpt (a/object-named "main_sign") Animator)]
  (.SetTrigger sign-anim "turn_on_sign")
  (.Play (a/cmpt (a/object-named "neon_on") AudioSource))))

(defn remove-diagram []
 (let [diagram (a/object-named "diagram")
       syd (a/object-named "sydney")]
  (timeline*
   (AND
    (tween {:position (l/v3 0 10 5)} diagram 1.5 {:in :pow3})
    (tween {:position (l/v3 0 2 4)} syd 2 {:in :pow3 :out :pow3}))
   (tween {:scale (l/v3 0 0 0)} diagram 0.5)
   #(do (a/destroy diagram) false))))

(defn spawn-diagram []
 (let [prefab (Resources/Load "Prefabs/brain_diagram")
       diagram (UnityEngine.Object/Instantiate prefab (l/v3 0 10 5) Quaternion/identity)
       syd (a/object-named "sydney")]
  (set! (.name diagram) "diagram")
  (timeline [(tween {:position (l/v3 0 2.3 5)} diagram 3 {:out :pow3})])
  (timeline [(wait 2)
             (tween {:position (l/v3 2 2.3 4)} syd 2 {:in :pow3 :out :pow3})])))

(defn spawn-biff []
 (let [prefab (Resources/Load "Prefabs/biff")
       biff (UnityEngine.Object/Instantiate
             prefab
             (.. (a/object-named "biffPos") transform position)
             (.. (a/object-named "biffPos") transform rotation))]
  (set! (.name biff) "biff")
  (a/hook+ biff :update
   (fn [go]
    (.LookAt (.transform biff) (.. Camera/main transform position))))))

(defn biff-charge []
 (let [biff (a/object-named "biff")]
  (timeline*
   (tween {:position (l/v3+ (l/v3* (.. Camera/main transform forward) 1.5) (.. Camera/main transform position))} biff 1 {:in :pow2 :out :pow3}))))

(defn biff-jump []
 (let [biff (a/object-named "biff")
       biff-bod (a/object-named "biffBody")
       orig-pos (.. biff transform position)]
  (timeline*
   (AND
    (tween {:position (l/v3+ orig-pos (l/v3 0 3 0))} biff 0.5 {:out :pow3})
    (tween {:material {:color (Color. 1 0 0 1)}} biff-bod 2 {:in :pow3 :out :pow3})))))

(defn go-black []
 (let [quad (a/cmpt (a/object-named "Quad") Renderer)]
  (set! (.enabled quad) true)))

(defn play [go]
 (let [audio (a/cmpt go AudioSource)]
  (.Play audio)))

(defn stop [go]
 (let [audio (a/cmpt go AudioSource)]
  (.Stop audio)))

(defn init-scene [_]
 (sydney-hover)
 (sydney-entrance)
 (timeline
  [(wait 3.0)
   #(do (turn-on-sign) false)
   #(do (play (a/object-named "intro")) false)  
   (wait 6.0)
   #(do (emote :greet 4) false)
   (wait 14.0)
   #(do (emote :pantomime 2) false)
   (wait 4.0)
   #(do (play (a/object-named "education")) false)
   (wait 2.0)
   #(do (spawn-diagram) false)
   (wait 16.0)
   #(do (remove-diagram) false)
   (wait 3.0)
   #(do (play (a/object-named "interface_intro")) false)
   (wait 1.0)
   #(do (emote :aside 2) false)
   (wait 8.0)
   #(do (play (a/object-named "distortion1")) false)
   #(do (play (a/object-named "distortion")) false)
   #(do (glitch/set-depth-clearing :nothing) false)
   (wait 4.0)
   #(do (glitch/set-depth-clearing :skybox) false)
   #(do (stop (a/object-named "distortion")) false)
   (wait 4.0)
   #(do (ui/add-interface) false)
   (wait 1)
   #(do (ui/add-button (fn [])) false)
   (wait 0.5)
   #(do (ui/add-button (fn [])) false)
   (wait 1.0)
   #(do (play (a/object-named "close_interface")) false)
   (wait 3.0)
   #(do (play (a/object-named "distortion")) false)
   #(do (glitch/set-depth-clearing :nothing) false)
   (wait 2.0)
   #(do (glitch/set-depth-clearing :skybox) false)
   #(do (stop (a/object-named "distortion")) false)
   (wait 4.0)
   #(do (ui/remove-interface) false)
   (wait 2.0)
   #(do (play (a/object-named "memory_backup_conditions")) false)
   (wait 5)
   #(do (emote :pantomime 4.0) false)
   (wait 7)
   #(do (emote :greet 4.0) false)
   (wait 4)
   #(do (play (a/object-named "memory_wipe_continue")) false)
   (wait 6)
   #(do (spawn-biff) false)
   (wait 8)
   #(do (play (a/object-named "oh_shit")) false)
   (wait 2)
   #(do (play (a/object-named "biff_activate")) false)
   #(do (biff-jump) false)
   (wait 3)
   #(do (play (a/object-named "good_day")) false)
   (wait 2)
   #(do (biff-charge) false)
   #(do (play (a/object-named "biff_charge")) false)
   (wait 0.5)
   #(do (go-black) false)
   #(do (play (a/object-named "hit")) false)
   #(do (play (a/object-named "breathing")) false)
   (wait 3)
   #(do (play (a/object-named "get_data")) false)
   (wait 2)
   #(do (play (a/object-named "biff_ok")) false)
   (wait 1.5)
   #(do (play (a/object-named "drag")) false)
   (wait 8.0)
   #(do (Application/Quit) false)]))
   
 