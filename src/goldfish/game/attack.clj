(ns goldfish.game.attack
  (:require [goldfish.game.unit :refer :all]
            [goldfish.game.effects :as fx]
            [goldfish.game.core :refer [update-unit get-unit unit-damage-change]]
            [goldfish.game.log-utils :refer :all]
            [goldfish.game.events :refer [damage-change]]))

(defn attack [attacker target]
  (damage target (:attack attacker)))

(defn attack-handler
  "Deal damage to attacker and target units."
  [fx s {:keys [attacker target]}]
  (let [a (get-unit s attacker)
        t (get-unit s target)]
    (println (unit->str a) "attacks" (unit->str t) "...")
    (let [next-s (-> s
                   (update-unit target attack a)
                   (update-unit attacker attack t))
          dmg-entries (map #(unit-damage-change s next-s %) [attacker target])
          next-e (assoc (damage-change) :units (into {} dmg-entries))]
      [next-s next-e])))
