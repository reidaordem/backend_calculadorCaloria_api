
(ns api-projeto.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clj-http.client	:as	http-client]
            [cheshire.core :refer :all]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))



(def api-key-alimento "GnbtPXjp29N6aP7eYSnb4wdL3FGKO863ANSurC7N")

(def usuario (atom nil))
(def extratos (atom ()))
 
(defroutes app-routes
  (GET "/saldocal" )
  (GET "/dados" )
  (GET "/extrato"
  (response (alimento @extratos)) )

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


    (defn alimento [extratos]
    (map (fn [item] (let[{ nome-alimento :nome qtd-alimento :quantidade} item ])) extratos)
    )

    


    (defn calcular-saldo [lista-extratos]
  (reduce (fn [acumulado item]
            (if (= (:tipo item) :ganho)
              (+ acumulado (:calorias item))   ;; Se for ganho, soma
              (- acumulado (:calorias item))))  ;; Se for perda, subtrai
          0
          lista-extratos))





  (defn calcula-caloria-alimento [nome quantidade]
  ;; Aqui você faria a chamada HTTP para a API do USDA usando o 'nome'
  ;; Vamos simular que a API retornou que 1 unidade/g tem 4 calorias:
  (let [caloria-unitaria 4] 
    (* caloria-unitaria quantidade)))

(defn calcula-caloria-exercicio [nome quantidade]
  ;; Aqui seria a lógica para exercícios (ex: minutos de corrida)
  ;; Vamos simular que 1 minuto de 'Corrida' gasta 10 calorias:
  (let [caloria-por-minuto 10]
    (* caloria-por-minuto quantidade)))


  (defn obter-calorias-por-nome [api-key nome-alimento]
  (let [url "https://usda.gov"
        ;; Faz a busca enviando a chave e o nome do alimento como parâmetros
        resposta (client/get url {:query-params {"api_key" api-key
                                                 "query" nome-alimento}
                                  :as :json})
        ;; Pega o primeiro alimento da lista de resultados
        primeiro-alimento (first (get-in resposta [:body :foods]))
        nutrientes (:foodNutrients primeiro-alimento)]
    
    ;; Filtra os nutrientes do alimento para achar a caloria (ID 208 ou "Energy")
    (-> (filter (fn [n] 
                  (or (= (:nutrientId n) 208)
                      (= (:nutrientName n) "Energy"))) 
                nutrientes)
        first
        :value)))


        (POST "/alimento" request
  (let [dados (:body request)
        calorias (calcula-caloria-alimento
                   (:alimento dados)
                   (:quantidade dados))

        registro (assoc dados :calorias calorias)]

    (swap! extratos conj registro)

    (response registro)))



    (defn registrar-alimento [dados]
  (let [nome (:nome dados)
        quantidade (:quantidade dados)

        calorias
        (obter-calorias-por-nome chave nome)

        extrato
        (assoc dados
               :calorias calorias
               :tipo :ganho)]

    (swap! extratos conj extrato)

    extrato))
