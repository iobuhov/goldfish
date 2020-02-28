(ns goldfish.game.core
  (:require
   [clojure.data            :refer [diff]]
   [goldfish.game.unit      :as unit]
   [goldfish.game.effects   :refer [fx-entry]]
   [goldfish.game.log-utils :as log]))

;; standard effects
(def attack-fx (fx-entry :attack :attack-handler))
(def death-fx  (fx-entry :damage-change :death-handler))

(def effects  [attack-fx death-fx])
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

(defn get-units [s]
  "Return state units coll."
  (vals (:units s)))

(defn unit-damage-change [s1 s2 uid]
  (let [prev (get-unit s1 uid)
        next (get-unit s2 uid)
        amount (apply - (map :damage [next prev]))]
    (println (log/unit->str next) "lost" amount "...")
    {uid amount}))

(defn get-dead-units
  "Return dead units coll."
  [s]
  (filter unit/is-dead? (get-units s)))

(defn drop-dead-units
  "Remove all dead units from state."
  [state]
  (let [dead-uids (map :id (get-dead-units state))]
    ;; (println (get-dead-units state))
    ;; (println dead-uids)
    ;; (println "REAL DEAD" (log/unit->str (get-unit state (first dead-uids))))
    ;; (clojure.pprint/pprint state)
    (reduce
     (fn [s id]
       (println "[DESTROING]" (log/unit->str (get-unit s id)) "...")
       (-> s
           (remove-unit id)
           (detach-unit :px id)
           (detach-unit :py id)))
     state
     dead-uids)))

(defn get-removed-units
  "Return units that was removed using two state snapshots."
  [s1 s2]
  (vals (first (diff (:units s1) (:units s2)))))

;; (let [w (unit/damage (unit/wolf) 4)
;;       u (unit/damage (unit/unicorn) 9)
;;       b (unit/berserk)
;;       s (-> state
;;             (add-unit w)
;;             (add-unit u)
;;             (add-unit b))]
;;   (get-dead-units s))

;; (let [w (unit/wolf)
;;       u (unit/unicorn)
;;       b (unit/berserk)
;;       s (-> state
;;             (add-unit w)
;;             (add-unit u)
;;             (add-unit b))]
;;   (get-removed-units s (remove-unit (remove-unit s (:id u)) (:id b))))

(clojure.pprint/pprint
 (let [w (unit/damage (unit/wolf) 4)
       u (unit/damage (unit/unicorn) 9)
       b (unit/berserk)
       s (-> state
             (put-unit :px w)
             (put-unit :py u)
             (put-unit :px b))]
   (drop-dead-units s)))

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

