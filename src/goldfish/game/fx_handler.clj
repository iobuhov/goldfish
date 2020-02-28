(ns goldfish.game.fx-handler
  (:require [goldfish.game.rules :refer [attack-handler]]))

(defmulti handler (fn [fx s e] (:fn fx)))

(defmethod handler :attack-handler [fx s e] (attack-handler fx s e))
