(ns goldfish.game.run)

(def boarding-event {:name :boardingn :runned-hs #{}})
(def attack-event   {:name :attack    :runned-hs #{}})
(def damage-event   {:name :damage    :runned-hs #{}})
(def state
  {:handlers
   [{:damage   :berserk-rage  :handler-id "berserk::01"}
    {:boarding :juggler-shoot :handler-id "juggler::01"}
    {:boarding :juggler-shoot :handler-id "juggler::02"}]})

(defn event? [event] (keyword? (:name event)))

(defn match-event? [event handler]
  (handler (:name event)))

(defn executed? [event handler]
  (not (nil? ((:runned-hs event) (:handler-id handler)))))

(defn has-handlers? [state event]
  (some #(match-event? event %) (:handlers state)))

(defn find-handlers [state event]
  (->> state
       (:handlers)
       (filterv #(match-event? event %))
       (filterv #(not (executed? event %)))))

(defn get-handler [state event]
  (when (and (event? event)
             (has-handlers? state event))
    (first (find-handlers state event))))

;; --------------------------------------------------

(executed? event {:handler-id "some-buddy"})
(has-handlers? state {:name :boarding})
(find-handlers state event)
(get-handler state event)

;; --------------------------------------------------

(defn run [state event]
  (loop [s state e event]
    (if (nil? e) s)))

;; (let [handler (get-reducer state event)
;;       [next-state next-event] (reducef state event)]
;;   (if (nil? next-event)
;;     next-state
;;     (recur next-state next-event)))
