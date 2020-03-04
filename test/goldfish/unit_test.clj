(ns goldfish.unit-test
  (:require
   [clojure.test :refer :all]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [clojure.test.check.clojure-test :refer [defspec]]
   [goldfish.game.unit :as unit]))


(def no-zero-nat
  (gen/fmap inc gen/nat))

(def unit-stats
  (gen/tuple
   gen/nat
   no-zero-nat))

(comment
  (gen/sample unit-stats))

(def bodies
  (gen/fmap
   (fn [[a h]] (unit/body a h))
   unit-stats))

(comment
  (gen/sample bodies))

(def body-set
  (gen/vector bodies 1 100))

(def hp-dmg
  (gen/fmap
   sort
   (gen/tuple no-zero-nat no-zero-nat)))

(comment
  (gen/sample hp-dmg))

(defspec body-is-pure-fn 100
  (prop/for-all
   [[a h] unit-stats]
   (let [b (unit/body a h)]
     (and (= (:attack b) a)
          (= (:health b) h)
          (= (:base-attack b) a)
          (= (:base-health b) h)))))

(defspec all-units-are-distinct 100
  (prop/for-all
   [bs body-set]
   (= (count bs)
      (count (set (map :id bs))))))

(defspec dead-if-damage-greater-then-health 100
  (prop/for-all
   [[h d] hp-dmg]
   (-> (unit/body h h)
       (unit/damage d)
       (unit/is-dead?))))

(comment
  (count (set [1 2 3])))
