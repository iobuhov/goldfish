(ns goldfish.game.log-utils)

(defn unit->str [u]
  (str (:name u) ":" (:id u)))
