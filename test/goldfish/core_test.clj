(ns goldfish.core-test
  (:require
   [clojure.test :refer :all]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.string :as str]
   [goldfish.game.core :as gc]))

;; (gen/vector)
;; (gen/small-integer)

(comment
  (def property
    (prop/for-all [v (gen/vector gen/small-integer)]
                  (let [s (sort v)]
                    (and (= (count v) (count s))
                         (or (empty? s)
                             (apply <= s)))))))

(comment
  (tc/quick-check 100 property))

(comment
  (defspec beginning 100
    (prop/for-all [] true)))

(comment
  (remove-ns 'goldfish.core-test))

(comment (str/upper-case "goldfish"))
;; uids - count is `l - 1`
;; uids is change only for player `p`
;; (defspec detach-is-state-reduce
;;   (prop/for-all []))

(defspec length-dosent-change
  (prop/for-all
   [s gen/string-ascii]
   (= (count s) (count (str/upper-case s)))))

(defspec everything-uppercased
  (prop/for-all
   [s gen/string-ascii]
   (every? #(if (Character/isLetter %)
              (Character/isUpperCase %)
              true)
           (str/upper-case s))))

(defspec idempotent
  (prop/for-all
   [s gen/string-ascii]
   (= (str/upper-case s)
      (str/upper-case (str/upper-case s)))))

(comment (gen/sample gen/string 4))
