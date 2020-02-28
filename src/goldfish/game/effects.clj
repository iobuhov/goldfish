(ns goldfish.game.effects
  (:require [nano-id.core :refer [nano-id]]))

(defn fx-entry [t f]
  {:id (nano-id 5) :fn f :trigger t})
