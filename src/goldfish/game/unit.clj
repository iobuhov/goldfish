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

;; (set-attack u 1)

(defn add-buff
  "Attach buff to unit."
  [u b]
  (update u :buffs conj b))

;; (add-buff (body 1 1) [:attack 2])

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

(defn give-+1-attack[u]
  "Give unit +1 attack bonus."
  (update u :buffs conj {:attack {"+" 1}}))

;; ------------------------------------------------------------

(defn wolf []
  (assoc (body 2 2) :name :wolf))

;; ------------------------------------------------------------

(defn unicorn []
  (assoc (body 3 5) :name :unicorn))

;; ------------------------------------------------------------

(defn berserk []
  (assoc (body 2 4) :name :berserk))

;; (defn rage [u n]
;;   (unit/add-buff u (buff/attack n)))

;; (defmethod fx/handler :berserk-rage
;;   [fx state {:keys [injured]}]
;;   (vec (update-unit state (:uid fx) rage (count injured))))

;; ------------------------------------------------------------

(defn knife-juggler []
  (assoc (body 2 2) :name :knife-juggler))

;; ------------------------------------------------------------

(defmulti create identity)
(defmethod create :wolf          [_] (wolf))
(defmethod create :unicorn       [_] (unicorn))
(defmethod create :berserk       [_] (berserk))
(defmethod create :knife-juggler [_] (knife-juggler))

;; ------------------------------------------------------------

;; (is-dead?
;;  (-> (body 1 10)
;;      (damage 2)
;;      (damage 5)
;;      (damage 1)))

