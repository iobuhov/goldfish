(ns goldfish.game.run
  (:require
   [goldfish.game.core :refer [state add-unit]]
   [goldfish.game.unit :refer [wolf unicorn]]
   [goldfish.game.fx-handler :refer [handler] :rename {handler fx-handler}]
   [goldfish.game.events :as evs]))

(defn event?
  "Check weather provided map is event."
  [e]
  (keyword? (:name e)))

(defn match-event?
  "Return `true` if event is a trigger for provided effect."
  [event fx]
  (= (:name event) (:trigger fx)))

(defn executed?
  "Return `ture` if effect has been called for this event."
  [event fx]
  (not (nil? ((:executed-fxs event) (:id fx)))))

(defn has-fxs?
  "Return `true` if state hase effects for the event."
  [state event]
  (some #(match-event? event %) (:effects state)))

(defn find-fxs
  "Return vector with all effect-entries that should be triggered for this event."
  [state event]
  (->> state
       (:effects)
       (filterv #(match-event? event %))
       (filterv #(not (executed? event %)))))

(defn get-fx
  "Pull first effect-entry from state that match given event."
  [state event]
  (when (and (event? event)
             (has-fxs? state event))
    (first (find-fxs state event))))

(defn reg-runned-fx
  "Register fx-entry as executed for given event."
  [event fx]
  (update event :executed-fxs conj (:id fx)))

(defn run-fx [fx st ev]
  "Execute fx for given fx-entry with given state & evnet.
   Return vector `[r e]` where `r` is a result of handler execution and `e`
   is event that marked as 'executed' for given `fx`."
  [(fx-handler fx st ev) (reg-runned-fx ev fx)])

(defn run [state event]
  (loop [s state ev-stack (list event)]
    ;; (clojure.pprint/pprint )
    (if-let [ev (first ev-stack)]
      (do
        (println "New" (:name (first ev-stack)) "event on the stack!")
        (if-let [fx (get-fx s ev)]
          (let [[fx-result after-event] (run-fx fx s ev)
                [next-st  next-ev]      fx-result
                next-stack              (conj (rest ev-stack) after-event)]
            (if next-ev
              (recur next-st (conj next-stack next-ev))
              (recur next-st next-stack)))
          (recur s (rest ev-stack))))
      (do
        (println "Event stack is empty...")
        (println "Return state:")
        (clojure.pprint/pprint s)
        s))))

;; --------------------------------------------------

(let [wx (wolf)
      wy (wolf)
      uc (unicorn)]
  (run
    (-> state
        (add-unit wx)
        (add-unit wy)
        (add-unit uc))
    (evs/attack :px (:id wx) (:id uc))))

;; --------------------------------------------------

;; (match-event? boarding-event juggler-fx1)
;; (match-event? attack-event   juggler-fx1)
;; (match-event? damage-event   juggler-fx1)

;; (has-fxs? state {:name :damage})
;; (has-fxs? state {:name :attack})
;; (has-fxs? state {:name :boarding})

;; (find-fxs state damage-event)
;; (find-fxs state boarding-event)
;; (find-fxs state attack-event)

;; (get-fx   state damage-event)
;; (get-fx   state boarding-event)
;; (get-fx   state attack-event)

;; --------------------------------------------------

;; (defmulti handler (fn [fx s e] (:fn fx)))

;; (defmethod handler :berserk-rage
;;   [fx s e]
;;   ;; (println (:fn fx) (:id fx))
;;   (println "Berserk see damage, his rage starts to grow...")
;;   [s])

;; (defmethod handler :juggler-shoot
;;   [fx s e]
;;   ;; (println (:fn fx) (:id fx))
;;   (println (str "Juggler " (:id fx) " shoot a unit..."))
;;   [s damage-event])

;; (handler (get-in state [:effects 0]) 1 damage-event)
;; (handler (get-in state [:effects 1]) 1 damage-event)
;; (handler (get-in state [:effects 2]) 1 damage-event)

;; --------------------------------------------------


;; (reg-runned-fx damage-event berserk-fx1)

;; --------------------------------------------------



;; (clojure.pprint/pprint (run-fx juggler-fx1 state boarding-event))

;; (run state boarding-event)
