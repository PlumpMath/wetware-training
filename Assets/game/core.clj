(ns game.core
 (require [arcadia.core :as a]
          [arcadia.linear :as l]
          [tween.core :refer :all]
          [game.ui :as ui]
          [game.glitch :as glitch])
 (import [UnityEngine Animator AudioSource Resources Quaternion Renderer Color RectTransform]))

(defn emote [emotion wait-duration]
 (let [syd-bod (a/object-named "sydneyBody")
       syd (a/object-named "sydney")
       orig-pos (.. syd transform position)
       orig-color (.color (.material (a/cmpt syd-bod Renderer)))]
  (a/set-state! syd :moving true)
  (cond
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
     (tween {:material {:color orig-color}} syd-bod 1 {:in :pow3 :out :pow3}))
    #(a/set-state! syd :moving false))
   (= emotion :pantomime)
   (timeline*
    (AND
     (tween {:position (l/v3+ (l/v3 0 1 0) orig-pos)} syd 1 {:in :pow3 :out :pow3})
     (tween {:material {:color (Color. 0.6 0.6 0.2 1)}} syd-bod 1 {:in :pow3 :out :pow3}))
    (wait wait-duration)
    (AND
     (tween {:position orig-pos} syd 1 {:in :pow3 :out :pow3})
     (tween {:material {:color orig-color}} syd-bod 1 {:in :pow3 :out :pow3}))
    #(a/set-state! syd :moving false)))))
   
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

(defn init-scene [_]
 (sydney-hover)
 (sydney-entrance)
 (timeline
  [(wait 3.0)
   #(do (turn-on-sign) false)
   (wait 8.0)
   #(do (spawn-diagram) false)
   (wait 8.0)
   #(do (remove-diagram) false)
   (wait 5.0)
   #(do (emote :aside 2) false)
   (wait 7.0)
   #(do (emote :pantomime 2) false)
   (wait 7.0)
   #(do (ui/add-interface) false)
   (wait 1)
   #(do (ui/add-button (fn [])) false)
   (wait 0.5)
   #(do (ui/add-button (fn [])) false)
   (wait 7.0)
   #(do (ui/remove-interface) false)
   (wait 3.0)
   #(do (glitch/set-depth-clearing :nothing) false)
   (wait 3.0)
   #(do (glitch/set-depth-clearing :skybox) false)]))
 