(ns goldfish.game.run
  (:require [nano-id.core :refer [nano-id]]))

(defn fx-entry [t f]
  {:id      (nano-id 5)
   :fn      f
   :trigger t})

(defn event [n]
  {:name      n
   :runned-hs #{}})

(def boarding-event (event :boarding))
(def attack-event   (event :attack))
(def damage-event   (event :damage))

(def berserk-fx1    (fx-entry :damage   :berserk-rage))
(def juggler-fx1    (fx-entry :boarding :juggler-shoot))
(def juggler-fx2    (fx-entry :boarding :juggler-shoot))
(def attack-fx      (fx-entry :attack   :combat))

(def state
  {:effects [berserk-fx1 juggler-fx1 juggler-fx2]})

(defn event? [event] (keyword? (:name event)))

(defn match-event? [event fx]
  (= (:name event) (:trigger fx)))

(defn executed? [event fx]
  (not (nil? ((:runned-hs event) (:id fx)))))

(defn has-fxs? [state event]
  (some #(match-event? event %) (:effects state)))

(defn find-fxs [state event]
  (->> state
       (:effects)
       (filterv #(match-event? event %))
       (filterv #(not (executed? event %)))))

(defn get-fx [state event]
  (when (and (event? event)
             (has-fxs? state event))
    (first (find-fxs state event))))

;; --------------------------------------------------

(match-event? boarding-event juggler-fx1)
(match-event? attack-event   juggler-fx1)
(match-event? damage-event   juggler-fx1)

(has-fxs? state {:name :damage})
(has-fxs? state {:name :attack})
(has-fxs? state {:name :boarding})

(find-fxs state damage-event)
(find-fxs state boarding-event)
(find-fxs state attack-event)

(get-fx   state damage-event)
(get-fx   state boarding-event)
(get-fx   state attack-event)

;; --------------------------------------------------

(defmulti handler (fn [fx s e] (:fn fx)))

(defmethod handler :berserk-rage
  [fx s e]
  ;; (println (:fn fx) (:id fx))
  (println "Berserk see damage, his rage starts to grow...")
  [s])

(defmethod handler :juggler-shoot
  [fx s e]
  ;; (println (:fn fx) (:id fx))
  (println (str "Juggler " (:id fx) " shoot a unit..."))
  [s damage-event])

(handler (get-in state [:effects 0]) 1 damage-event)
(handler (get-in state [:effects 1]) 1 damage-event)
(handler (get-in state [:effects 2]) 1 damage-event)

;; --------------------------------------------------

(defn reg-runned-fx [event fx]
  (update event :runned-hs conj (:id fx)))

(reg-runned-fx damage-event berserk-fx1)

;; --------------------------------------------------

(defn run-fx [fx st ev]
  [(handler fx st ev) (reg-runned-fx ev fx)])

(clojure.pprint/pprint (run-fx juggler-fx1 state boarding-event))

(defn run [state event]
  (loop [s state ev-stack (list event)]
    ;; (println (mapv :name ev-stack))
    (if-let [ev (first ev-stack)]
      (if-let [fx (get-fx s ev)]
        (let [[fx-result after-event] (run-fx fx s ev)
              [next-st  next-ev]      fx-result
              next-stack              (conj (rest ev-stack) after-event)]
          (if next-ev
            (recur next-st (conj next-stack next-ev))
            (recur next-st next-stack)))
        (recur s (rest ev-stack)))
      state)))

(run state boarding-event)
