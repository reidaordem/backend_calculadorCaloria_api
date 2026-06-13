(ns api-projeto.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clj-http.client :as http-client]
            [ring.util.response :refer [response]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

;; ======================
;; Estado da aplicação
;; ======================

(def api-key-alimento "GnbtPXjp29N6aP7eYSnb4wdL3FGKO863ANSurC7N")

(def usuario (atom nil))
(def extratos (atom '()))

;; ======================
;; API USDA
;; ======================

(defn buscar-caloria-por-nome
  [api-key nome-alimento]

  (let [url "https://api.nal.usda.gov/fdc/v1/foods/search"

        resposta
        (http-client/get
          url
          {:query-params
           {"api_key" api-key
            "query" nome-alimento}

           :as :json})

        primeiro-alimento
        (first (get-in resposta [:body :foods]))]

    (->> (:foodNutrients primeiro-alimento)

         (filter
           #(= "Energy"
               (get-in %
                       [:nutrient :name])))

         first
         :amount)))

(defn calcula-caloria-alimento
  [nome quantidade]

  (let [calorias-100g
        (buscar-caloria-por-nome
          api-key-alimento
          nome)]

    (* calorias-100g
       (/ quantidade 100.0))))

;; ======================
;; Saldo
;; ======================

(defn calcular-saldo
  [lista-extratos]

  (reduce
    (fn [acumulado item]

      (if (= (:tipo item) :ganho)

        (+ acumulado (:calorias item))

        (- acumulado (:calorias item))))

    0
    lista-extratos))

;; ======================
;; Rotas
;; ======================

(defroutes app-routes

  ;; usuário

  (POST "/usuario" request

    (let [dados (:body request)]

      (reset! usuario dados)

      (response
        {:status 200
         :usuario @usuario})))

  ;; alimento

  (POST "/alimento" request

    (let [dados (:body request)

          calorias
          (calcula-caloria-alimento
            (:nome dados)
            (:quantidade dados))

          registro
          (assoc dados
                 :calorias calorias
                 :tipo :ganho)]

      (swap! extratos conj registro)

      (response registro)))

  ;; extrato

  (GET "/extrato" []

    (response @extratos))

  ;; saldo

  (GET "/saldocal" []

    (response
      {:saldo
       (calcular-saldo @extratos)}))

  (route/not-found
    {:erro "Rota não encontrada"}))

;; ======================
;; Middlewares
;; ======================

(def app
  (-> app-routes
      (wrap-json-body {:keywords? true})
      wrap-json-response
      (wrap-defaults site-defaults)))
