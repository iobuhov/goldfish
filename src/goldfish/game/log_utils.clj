(ns goldfish.game.log-utils
  (:require
   [clojure.string :as stng]))

(defn unit->str [u]
  (str "<" (stng/upper-case (name (:name u))) " id=\"" (:id u) "\"" ">"))

(defn spacer []
  (println)
  (println "--------------------------------------------------")
  (println))
