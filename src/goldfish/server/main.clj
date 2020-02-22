(ns goldfish.server.main
  (:require
   [clojure.string     :as str]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
   [ring.middleware.reload :as reload]
   [ring.middleware.anti-forgery :as anti-forgery]
   [compojure.core     :as comp :refer (defroutes GET POST)]
   [compojure.route    :as route]
   [ring.util.response :as response]
   [hiccup.core        :as hiccup]
   [clojure.core.async :as async  :refer (<! <!! >! >!! put! chan go go-loop)]
   [taoensso.encore    :as encore :refer (have have?)]
   [taoensso.timbre    :as timbre :refer (tracef debugf info infof warnf errorf)]
   [taoensso.sente     :as sente]
   [org.httpkit.server :as http-kit]
   [taoensso.sente.server-adapters.http-kit :refer (get-sch-adapter)]
   [taoensso.sente.packers.transit :as sente-transit]))



(reset! sente/debug-mode?_ true) ; Uncomment for extra debug info



;; sente boilerplate
(let [;; Serializtion format, must use same val for client + server:
      packer :edn ; Default packer, a good choice in most cases
      ;; (sente-transit/get-transit-packer) ; Needs Transit dep

      chsk-server
      (sente/make-channel-socket-server!
       (get-sch-adapter) {:packer packer})

      {:keys [ch-recv send-fn connected-uids
              ajax-post-fn ajax-get-or-ws-handshake-fn]}
      chsk-server]

  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv) ; ChannelSocket's receive channel
  (def chsk-send!                    send-fn) ; ChannelSocket's send API fn
  (def connected-uids                connected-uids) ; Watchable, read-only atom
  )

(add-watch
 connected-uids :connected-uids
 (fn [_ _ old new]
   (when (not= old new)
     (infof "Connected uids change: %s" new))))

;; (defn landing-pg-handler [req]
;;   (response/content-type
;;    (response/resource-response "public/index.html")
;;    "text/html"))

(defn login-handler
  "Here's where you'll add your server-side login/auth procedure (Friend, etc.).
  In our simplified example we'll just always successfully authenticate the user
  with whatever user-id they provided in the auth request."
  [ring-req]
  (let [{:keys [session params]} ring-req
        {:keys [user-id]} params]
    (debugf "Login request: %s" params)
    {:status 200 :session (assoc session :uid user-id)}))

(defn landing-pg-handler [ring-req]
  (hiccup/html
    (let [csrf-token
          ;; (:anti-forgery-token ring-req) ; Also an option
          (force anti-forgery/*anti-forgery-token*)]

      [:div#sente-csrf-token {:data-csrf-token csrf-token}])
    [:div#app
     [:h1 "Loading..."]]
    [:script {:src "js/compiled/goldfish.js"}] ; Include our cljs target
    ))

(defroutes ring-routes
  (GET  "/"      ring-req (landing-pg-handler            ring-req))
  (GET  "/chsk"  ring-req (ring-ajax-get-or-ws-handshake ring-req))
  (POST "/chsk"  ring-req (ring-ajax-post                ring-req))
  (POST "/login" ring-req (login-handler                 ring-req))
  (route/resources "/") ; Static files, notably public/main.js (our cljs target)
  (route/not-found "<h1>Page not found</h1>"))

(def main-ring-handler (wrap-defaults (reload/wrap-reload ring-routes) site-defaults))

(defonce counter (ref 0))

(defn cinc []
  (dosync
   (alter counter inc)))

(defn cdec []
  (dosync
   (alter counter dec)))

(defmulti -event-msg-handler
  "Multimethod to handle Sente `event-msg`s"
  :id ; Dispatch on event-id
  )

(defmethod -event-msg-handler :goldfish/counter-inc
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid     (:uid     session)]
    (infof "%s is asking to inc counter" uid)
    (cinc)
    (chsk-send! uid [:goldfish/counter-inc (str @counter)])))

(defmethod -event-msg-handler :goldfish/counter-dec
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid     (:uid     session)]
    (infof "%s is asking to dec counter" uid)
    (cdec)
    (chsk-send! uid [:goldfish/counter-dec (str @counter)])))

(defmethod -event-msg-handler
  :default ; Default/fallback case (no other matching handler)
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid     (:uid     session)]
    (debugf "Unhandled event: %s" event)
    (when ?reply-fn
      (?reply-fn {:umatched-event-as-echoed-from-server event}))))

(defn event-msg-handler
  "Wraps `-event-msg-handler` with logging, error catching, etc."
  [{:as ev-msg :keys [id ?data event]}]
  (info "ev-msg id" id)
  (-event-msg-handler ev-msg) ; Handle event-msgs on a single thread
  ;; (future (-event-msg-handler ev-msg)) ; Handle event-msgs on a thread pool
  )

;;;; Sente event router (our `event-msg-handler` loop)
;; bull sheet with start router -- wtf?
(defonce router_ (atom nil))
(defn  stop-router! [] (when-let [stop-fn @router_] (stop-fn)))
(defn start-router! []
  (stop-router!)
  (reset! router_
          (sente/start-server-chsk-router!
           ch-chsk event-msg-handler)))

;; starting server
(defonce    web-server_ (atom nil)) ; (fn stop [])
(defn  stop-web-server! [] (when-let [stop-fn @web-server_] (stop-fn)))
(defn start-web-server! [& [port]]
  (stop-web-server!)
  (let [port (or port 0) ; 0 => Choose any available port
        ring-handler (var main-ring-handler)

        [port stop-fn]
        ;;; TODO Choose (uncomment) a supported web server ------------------
        (let [stop-fn (http-kit/run-server ring-handler {:port port})]
          [(:local-port (meta stop-fn)) (fn [] (stop-fn :timeout 100))])
        ;;
        ;; (let [server (immutant/run ring-handler :port port)]
        ;;   [(:port server) (fn [] (immutant/stop server))])
        ;;
        ;; (let [port (nginx-clojure/run-server ring-handler {:port port})]
        ;;   [port (fn [] (nginx-clojure/stop-server))])
        ;;
        ;; (let [server (aleph/start-server ring-handler {:port port})
        ;;       p (promise)]
        ;;   (future @p) ; Workaround for Ref. https://goo.gl/kLvced
        ;;   ;; (aleph.netty/wait-for-close server)
        ;;   [(aleph.netty/port server)
        ;;    (fn [] (.close ^java.io.Closeable server) (deliver p nil))])
        ;; ------------------------------------------------------------------

        uri (format "http://localhost:%s/" port)]

    (infof "Web server is running at `%s`" uri)
    (try
      (.browse (java.awt.Desktop/getDesktop) (java.net.URI. uri))
      (catch java.awt.HeadlessException _))

    (reset! web-server_ stop-fn)))

(defn stop!  []  (stop-router!)  (stop-web-server!))
(defn start! []  (start-router!) (start-web-server!))

(defn -main [] (start!))
;; (defn -main [& args]
;;   (let [port (some-> (or (first args) (env :http-port "3000"))
;;                      (Integer/parseInt))]
;;     (run-server #'handler {:port port})
;;     (println (str "Running server at http://localhost:" port ""))))
