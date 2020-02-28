(ns goldfish.game.buff
  (:require [goldfish.game.unit :refer [add-buff]]))

(defn attack
  "Create buff for attack."
  [n]
  [:rage :attack])

(defn impose [u buff])
