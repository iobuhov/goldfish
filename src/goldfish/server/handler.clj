(ns goldfish.server.handler
  (:require
   ;; [org.httpkit.server :refer [as-channel]]
   [environ.core :refer [env]]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
   [ring.middleware.reload :as reload]
   [ring.middleware.cors :as cors]
   [ring.util.response :as response]
   [compojure.core :refer :all]
   [compojure.route :as route]
   [taoensso.sente :as sente]
   [taoensso.sente.server-adapters.http-kit :refer (get-sch-adapter)]))

;; (defn app-async-handler [req])

;; (defn game-handler [req]
;;   (if-not (:websocket? req)
;;     {:status 200
;;      :headers {"Content-Type" "text/html"}
;;      :body "Welcome to the Goldfish! Waiting JS clients..."}
;;     (as-channel req
;;                 {:on-receive (fn [ch message] (println "on-receive:" message))
;;                  :on-close   (fn [ch status]  (println "on-close:"   status))
;;                  :on-open    (fn [ch]         (println "on-open:"    ch))})))

(let [{:keys [ch-recv send-fn connected-uids
              ajax-post-fn ajax-get-or-ws-handshake-fn]}
      (sente/make-channel-socket! (get-sch-adapter) {})]

  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv) ; ChannelSocket's receive channel
  (def chsk-send!                    send-fn) ; ChannelSocket's send API fn
  (def connected-uids                connected-uids) ; Watchable, read-only atom
  )

(defroutes app-routes
  (GET "/" []
       (response/content-type
        (response/resource-response "public/index.html")
        "text/html"))
  (GET  "/ws" req (ring-ajax-get-or-ws-handshake req))
  (POST "/ws" req (ring-ajax-post                req))
  ;; (GET "/ws" [] game-handler)
  (route/not-found "Not Found"))

(def handler
  (-> #'app-routes
      (cond-> (env :dev?) (reload/wrap-reload))
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))
      (cors/wrap-cors :access-control-allow-origin [#".*"]
                      :access-control-allow-methods [:get :put :post :delete]
                      :access-control-allow-credentials ["true"])))
