(ns back.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.cors :refer [wrap-cors]]
            [compojure.core :refer [defroutes GET POST DELETE]]
            [cheshire.core :as json]))

(def apostas (atom []))
(def id-aposta (atom 0))

(defn gerar-id-unico []
  (swap! id-aposta inc))

(defn listar-apostas []
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (json/encode @apostas)})

(defn deletar-aposta [id]
  (swap! apostas #(remove (fn [aposta] (= (:id aposta) (Integer/parseInt id))) %))
  {:status 200
   :body "Aposta removida com sucesso!"})

(defroutes app
  (GET "/apostas" [] (listar-apostas))
  (POST "/aposta" req
    (let [dados (json/decode (slurp (:body req)) true)
          aposta-com-id (assoc dados :id (gerar-id-unico))]
      (swap! apostas conj aposta-com-id)
      {:status 200
       :body "Aposta criada com sucesso!"}))
  (DELETE "/aposta/:id" [id]
    (deletar-aposta id)))

(def app-cors
  (wrap-cors app
             :access-control-allow-origin [#".*"]
             :access-control-allow-methods [:get :post :delete]))

(defn -main []
  (run-jetty app-cors {:port 5000 :join? false}))
