(ns ^:figwheel-always goldfish.client.main
  (:require-macros
   [cljs.core.async.macros :as asyncm :refer (go go-loop)])
  (:require
   [clojure.string  :as str]
   [cljs.pprint :refer [pprint]]
   [cljs.core.async :as async  :refer (<! >! put! chan)]
   [taoensso.encore :as encore :refer-macros (have have?)]
   [taoensso.timbre :as timbre :refer-macros (tracef debugf infof warnf errorf)]
   [taoensso.sente  :as sente  :refer (cb-success?)]
   [reagent.core :as r]
   [reagent.dom :as rdom])
  )

(enable-console-print!)

(def counter (r/atom nil))

(def ?csrf-token
  (when-let [el (.getElementById js/document "sente-csrf-token")]
    (.getAttribute el "data-csrf-token")))

(if ?csrf-token
  (println "CSRF token detected in HTML, great!")
  (println "CSRF token NOT detected in HTML, default Sente config will reject requests"))

(let [packer :edn
      {:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket-client!
       "/chsk" ; Must match server Ring routing URL
       ?csrf-token
       {:packer packer})]
  (def chsk       chsk)
  (def ch-chsk    ch-recv) ; ChannelSocket's receive channel
  (def chsk-send! send-fn) ; ChannelSocket's send API fn
  (def chsk-state state)   ; Watchable, read-only atom
)

(defmulti -event-msg-handler
  "Multimethod to handle Sente `event-msg`s"
  :id ; Dispatch on event-id
  )

(defmethod -event-msg-handler :default
  [{:as ev-msg :keys [event]}]
  (println "Unhandled event: ")
  (pprint event))

(defmethod -event-msg-handler :chsk/state
  [{:as ev-msg :keys [?data]}]
  (let [[old-state-map new-state-map] (have vector? ?data)]
    (if (:first-open? new-state-map)
      (do
        (println  "Channel socket successfully established!: ")
        (pprint new-state-map))
      (do
        (println "Channel socket state change: %s")
        (pprint new-state-map)))))

(defmethod -event-msg-handler :chsk/recv
  [{:as ev-msg :keys [?data]}]
  (println "Push event from server: ")
  (reset! counter (second ?data))
  (pprint ?data))

(defmethod -event-msg-handler :chsk/handshake
  [{:as ev-msg :keys [?data]}]
  (let [[?uid ?csrf-token ?handshake-data] ?data]
    (println "Handshake: ")
    (pprint ?data)))

(defn event-msg-handler
  "Wraps `-event-msg-handler` with logging, error catching, etc."
  [{:as ev-msg :keys [id ?data event]}]
  (-event-msg-handler ev-msg))

(defonce router_ (atom nil))
(defn  stop-router! [] (when-let [stop-f @router_] (stop-f)))
(defn start-router! []
  (stop-router!)
  (reset! router_
          (sente/start-client-chsk-router!
           ch-chsk event-msg-handler)))

(defn start! [] (start-router!))

(def started (atom false))
(def loggedin (r/atom false))

(defn start-once! []
  (when-not @started
    (reset! started true)
    (start!)))

(def click-count (r/atom 0))
(def username (r/atom ""))

(defn push-inc []
  (chsk-send! [:goldfish/counter-inc]))

(defn push-dec []
  (chsk-send! [:goldfish/counter-dec]))

(defn login-click-handler []
  (let [user-id @username]
    (sente/ajax-lite
     "/login"

     {:method :post
      :headers {:X-CSRF-Token (:csrf-token @chsk-state)}
      :params  {:user-id (str user-id)}}

     (fn [ajax-resp]
       (println "Ajax login response: " ajax-resp)
       (let [login-successful? true]
         (if-not login-successful?
           (println "Login failed")
           (do
             (println "Login successful")
             (reset! loggedin true)
             (start-once!))))))))

(defn login-form []
  [:div
   "User name please:"
   [:input {:type "text"
            :value @username
            :on-change #(reset! username (-> % .-target .-value))}]
   [:button {:disabled (> (count @username) 5)
             :on-click login-click-handler}
    "Login"]])

(defn counting-component []
  [:div
   "The atom " [:code "click-count"] " has value: "
   @click-count ". "
   [:input {:type "button" :value "Inc counter"
            :on-click push-inc}]
   [:input {:type "button" :value "Dec counter"
            :on-click push-dec}]])

(defn app []
  [:div
   [:h1 "Goldfish"]
   (if @loggedin
     [counting-component]
     [login-form])
   (when @counter
     [:div "Counter: " @counter])])

(rdom/render [app] (js/document.getElementById "app"))
