(ns goldfish.game.core)

(defn unit [attack health]
  {:attack attack
   :health health
   :damage 0})

(defn damage [u points]
  (if (> points 0)
    (update u :damage + points)
    u))

(defn is-dead? [u]
  (>= (:damage u) (:health u)))

(damage (damage (unit 1 10) 3) )

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
  (unit 2 2))

(defn unicorn []
  (unit 3 5))

(defn card-play
  [game player card]
  (condp = card
    :wolf (update-in game [:data player] #(put-on-board % (wolf)))
    :unicorn (update-in game [:data player] #(put-on-board % (unicorn)))))

(card-play game-state :Alice :wolf)
