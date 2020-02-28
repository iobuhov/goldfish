(ns goldfish.game.rules
  (:require
   [goldfish.game.unit      :refer :all]
   [goldfish.game.effects   :as fx]
   [goldfish.game.core      :as core :refer [update-unit get-unit unit-damage-change]]
   [goldfish.game.log-utils :refer :all]
   [goldfish.game.events    :refer [damage-change destroy]]))

(defn attack-handler
  "Deal damage to attacker and target units."
  [fx s {:keys [attacker target]}]
  ;; (clojure.pprint/pprint s)
  (let [a (get-unit s attacker)
        t (get-unit s target)]
    (spacer)
    (println "[ATTACK]")
    (println (unit->str a) "attacks" (unit->str t) "...")
    (let [next-s (-> s
                     (update-unit target attack a)
                     (update-unit attacker attack t))
          dmg-entries (map #(unit-damage-change s next-s %) [attacker target])
          next-e (assoc (damage-change) :units (into {} dmg-entries))]
      ;; (clojure.pprint/pprint next-s)
      [next-s next-e])))

(defn death-after-event
  "Take `prev` and `next` state and return :destroy event."
  [prev next]
  (let [removed-us (core/get-removed-units prev next)]
    (destroy removed-us)))

(defn death-handler
  "Remove unit from game state."
  [fx s {:keys [units]}]
  (spacer)
  (println "[REMOVING DEAD UNITS]")
  (let [next (core/drop-dead-units s)
        ev   (death-after-event s next)]
    [next ev]))
