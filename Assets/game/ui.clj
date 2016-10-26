(ns game.ui
 (require [arcadia.core :as a]
          [arcadia.linear :as l]
          [tween.core :refer :all])
 (import [UnityEngine RectTransform Vector3 Resources Quaternion AudioSource]
         CameraFacingBillboard))

(defn add-interface []
 (let [prefab (Resources/Load "Prefabs/Visor")
       start-pos (l/v3* (l/v3+ (.. Camera/main transform position) (.. Camera/main transform forward)) 2)
       visor (UnityEngine.Object/Instantiate prefab start-pos Quaternion/identity)
       sound (a/cmpt (a/object-named "menu_open") AudioSource)]
  (set! (.m_Camera (a/cmpt visor CameraFacingBillboard)) Camera/main)
  (set! (.. visor transform localScale) (l/v3 0 0 0))
  (set! (.name visor) "visor")
  (.Play sound)
  (timeline*
   (tween {:scale (l/v3 0.002 0.002 0.002)} visor 1 {:in :pow3 :out :pow3}))))

(defn remove-interface []
 (let [visor (a/object-named "visor")
       sound (a/cmpt (a/object-named "menu_close") AudioSource)]
  (.Play sound)
  (timeline*
   (tween {:scale (l/v3 0 0 0)} visor 0.5 {:in :pow3 :out :pow3})
   #(do (a/destroy visor) false))))

(defn add-button [activate-fn]
 (let [interface (a/object-named "Interface")
       prefab (Resources/Load "Prefabs/button")
       button (UnityEngine.Object/Instantiate prefab Vector3/zero (.. interface transform rotation))
       button-pos (.anchoredPosition3D (a/cmpt button RectTransform))] 
  (set! (.. button transform parent) (.transform interface))
  (set! (.. button transform localScale) (l/v3 1 1 1))
  (set!
   (.anchoredPosition3D (a/cmpt button RectTransform))
   (l/v3 (.x button-pos) (.y button-pos) -0.1))
  (set! (.. button transform localScale) (l/v3 0 0 0))
  (timeline*
   (tween {:scale (l/v3 1 1 1)} button 0.5 {:out :pow3}))
  (a/hook+ button :on-pointer-enter
   (fn [go ptr]
    (timeline*
     (tween {:scale (l/v3 1.2 1.2 1.2)} go 0.3 {:in :pow3 :out :pow3}))))
  (a/hook+ button :on-pointer-exit
   (fn [go ptr]
    (timeline*
     (tween {:scale (l/v3 1 1 1)} go 0.3 {:in :pow3 :out :pow3}))))))
