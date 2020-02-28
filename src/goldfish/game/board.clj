(ns goldfish.game.board
  (:require [goldfish.game.unit :refer :all]))

(defn put [state {:keys [unit player]}]
  (-> state
      (update :units conj unit)
      (update-in [player :uids] conj (:id unit))))

;; (let [s {:units [] :px {:uids #{}}}
;;       e {:unit (wolf) :player :px}]
;;   (put s e))
