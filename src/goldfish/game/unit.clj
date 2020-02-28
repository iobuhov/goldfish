(ns goldfish.game.unit
  (:require
   [nano-id.core :refer [nano-id]]
   [goldfish.game.effects :as fx]))

(defn body
  "Create map with basic unit attributes."
  [attack health]
  {:attack      attack
   :health      health
   :base-attack attack
   :base-health health
   :damage      0
   :buffs       []
   :id          (nano-id 10)})

(defn add-buff
  "Attach buff to unit."
  [u b]
  (update u :buffs conj b))

(defn damage
  "Add damage points to unit."
  [u points]
  (if (> points 0) (update u :damage + points) u))

(defn is-dead?
  "Return `true` if unit is dead."
  [u]
  (>= (:damage u) (:health u)))

(defn took-damag?
  "Compare previous and next state of unit. Return `true` if unit was injured."
  [prev next]
  (apply < (mapv :damage [prev next])))

(defn attack
  "Perform 'attack' on target unit."
  [attacker target]
  (damage target (:attack attacker)))

(defn wolf []
  (assoc (body 2 2) :name :wolf))

(defn unicorn []
  (assoc (body 3 5) :name :unicorn))

(defn berserk []
  (assoc (body 2 4) :name :berserk))

(defn knife-juggler []
  (assoc (body 2 2) :name :knife-juggler))

(defmulti create identity)
(defmethod create :wolf          [_] (wolf))
(defmethod create :unicorn       [_] (unicorn))
(defmethod create :berserk       [_] (berserk))
(defmethod create :knife-juggler [_] (knife-juggler))
