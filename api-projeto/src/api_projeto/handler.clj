(ns api-projeto.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clj-http.client	:as	http-client]
            [cheshire.core :refer :all]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defroutes app-routes
  (GET "/saldocal" )
  (GET "/dados" )
  (GET "/extrato" )

  (POST "/exercicio" request
    (let [dados (:body request)]
    (swap! extratos conj dados)
    (response {:status 200})
    ))

  (POST "/alimento" request
  (let [dados (:body request)]
    (swap! extratos conj dados)
    (response {:status 200})
    ))

  (POST "/usuario" request
  (let [dados (:body request)]
    (reset! usuario dados)
    (response {:status 200})
    ))

  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (wrap-json-body {:keywords? true})
      wrap-json-response
      (wrap-defaults site-defaults)))


  (def chave)
(def usuario (atom nil))
(def extratos (atom ()))


(defn)