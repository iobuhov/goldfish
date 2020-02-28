(ns goldfish.game.fx-handler
  (:require [goldfish.game.rules :refer [attack-handler death-handler]]))

(defmulti handler (fn [fx s e] (:fn fx)))

(defmethod handler :attack-handler [fx s e] (attack-handler fx s e))

(defmethod handler :death-handler [fx s e] (death-handler fx s e))
