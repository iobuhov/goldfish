(ns goldfish.game.events)

(defn attack
  "Create 'attack' event."
  [p a t]
  {:name         :attack
   :player       p
   :attacker     a
   :target       t
   :executed-fxs #{}})

(defn damage-change
  "Create 'damage-change' event."
  []
  {:name         :damage-change
   :executed-fxs #{}
   :units        {}
   :players      {}})
