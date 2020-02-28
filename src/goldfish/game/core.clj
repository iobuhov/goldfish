(ns goldfish.game.core
  (:require
   [goldfish.game.unit :as unit]
   [goldfish.game.effects :refer [fx-entry]]))

;; standard effects
(def attack-fx (fx-entry :attack :attack-handler))

(def effects  [attack-fx])
(def units    {})
(def player-x {:uids #{}})
(def player-y {:uids #{}})

(def state
  {:effects effects
   :units   units
   :px      player-x
   :py      player-y})

(defn attach-unit
  "Attach unit to the player"
  [s p uid]
  (update-in s [p :uids] conj uid))

(defn detach-unit
  "Remove unit from player's ownership"
  [s p uid]
  (update-in s [p :uids] disj uid))

(defn add-unit
  "Add unit to game state"
  [s u]
  (update s :units assoc (:id u) u))

(defn remove-unit
  "Remove unit from game state"
  [s uid]
  (update s :units dissoc uid))

(defn update-unit
  "Update unit using provided function"
  ([s uid f x] (update-in s [:units uid] f x))
  ([s uid f x y] (update-in s [:units uid] f x y))
  ([s uid f x y z] (update-in s [:units uid] f x y z)))

(defn get-unit
  "Get unit by id."
  [s uid]
  (get-in s [:units uid]))

(defn put-unit
  "'Put' unit on the 'board' and attach it to the player"
  [s p u]
  (-> s (add-unit u) (attach-unit p (:id u))))

(defn drop-unit
  "Remove unit from state and player->unit relation"
  [s p uid]
  (-> s (remove-unit uid) (detach-unit p uid)))

(defn unit-damage-change [s1 s2 uid]
  (let [prev (get-unit s1 uid)
        next (get-unit s2 uid)
        amount (apply - (map :damage [next prev]))]
    (println (:name next) "lost" amount "...")
    {uid amount}))
;; ------------------------------------------------------------
;; (assign-unit state :px "fudnfd")

;; (put-unit state :px (unit/berserk))
;; (put-unit state {:player :px :unit (unit/wolf)})

;; (clojure.pprint/pprint (let [u   (unit/unicorn)
;;                              uid (:id u)]
;;                          [uid (get-in (-> state
;;                                           (put-unit :px (unit/wolf))
;;                                           (put-unit :px (unit/wolf))
;;                                           (put-unit :px u)
;;                                           (put-unit :px (unit/berserk))
;;                                           (drop-unit :px uid)) [:px :uids])]))

