(ns goldfish.game.core)

(defn unit [attack health]
  {:attack attack
   :health health
   :damage 0
   :buffs  []})

(defn damage [u points]
  (if (> points 0)
    (update u :damage + points)
    u))

(defn is-dead? [u]
  (>= (:damage u) (:health u)))

(is-dead? (-> (unit 1 10)
              (damage 2)
              (damage 5)
              (damage 4)))

(def state-a
  {:player-id :Alice
   :health 30
   :board []})

(def state-b
  {:player-id :Bob
   :health 30
   :board []})

(def game-state {:game-id :Alice-vs-Bob
                 :players [:Alice :Bob]
                 :data {:Alice state-a
                        :Bob   state-b}})

(defn put-on-board
  [game-data x]
  (update game-data :board conj x))

(defn wolf []
  (assoc (unit 2 2) :name :wolf))

(defn unicorn []
  (assoc (unit 3 5) :name :unicorn))

(defn card-play
  [game player card]
  (condp = card
    :wolf (update-in game [:data player] #(put-on-board % (wolf)))
    :unicorn (update-in game [:data player] #(put-on-board % (unicorn)))))

(clojure.pprint/pprint (card-play game-state :Alice :wolf))

;; ------------------------------------------------------------
;;; Engine, Reflection, Rules

(defn berserk [] (assoc (unit 2 4) :name :berserk))

(defn took-damag?
  "Compare previous and next state of unit.
  Return `true` if unit should gain bonus."
  [prev next]
  (apply < (mapv :damage [prev next])))

(defn give-+1-attack[u]
  "Give unit +1 attack bonus."
  (update u :buffs conj {:attack {"+" 1}}))

(def b1 (berserk))
(def b2 (damage b1 2))

(def all-rules
  {:berserk berserk-rule-trigger})

(def state1 {:x      {:board []}
             :y      {:board []}
             :units  [:berserk]})
(defn get-units [state]
  (:units state))

(defn pull-berserk)
;; 1. game-loop
;; 2. recur?
;; 3. narrow
;;
;; (defn game-loop)
