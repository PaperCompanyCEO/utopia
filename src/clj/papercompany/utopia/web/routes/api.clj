(ns papercompany.utopia.web.routes.api
  (:require
   [papercompany.utopia.web.controllers.examples :as examples]
   [papercompany.utopia.web.controllers.cljs :as cljs]
   [papercompany.utopia.web.controllers.health :as health]
   [papercompany.utopia.web.middlewares.exception :as exception-middleware]
   [papercompany.utopia.web.middlewares.formats :as formats-middleware]
   [papercompany.utopia.web.middlewares.fx-cofx :as fx-cofx-middleware]
   [papercompany.utopia.web.middlewares.input :as input-middleware]
   [papercompany.utopia.web.middlewares.transactions :as transactions-middleware]
   [papercompany.utopia.specs.core :as specs]
   [integrant.core :as ig]
   [reitit.coercion.malli :as malli]
   [reitit.ring.coercion :as coercion]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.parameters :as parameters]
   [reitit.swagger :as swagger]))

(def route-data
  {:coercion   malli/coercion
   :muuntaja   formats-middleware/instance
   :swagger    {:id ::api}
   :middleware [;; query-params & form-params
                parameters/parameters-middleware
                  ;; content-negotiation
                muuntaja/format-negotiate-middleware
                  ;; encoding response body
                muuntaja/format-response-middleware
                  ;; exception handling
                coercion/coerce-exceptions-middleware
                  ;; decoding request body
                muuntaja/format-request-middleware
                  ;; coercing response bodys
                coercion/coerce-response-middleware
                  ;; coercing request parameters
                coercion/coerce-request-middleware
                ;; input snapshot
                input-middleware/input-middleware
                ;; fx-cofx
                fx-cofx-middleware/fx-cofx-middleware
                ;; transactions
                transactions-middleware/transactions-middleware
                ;; exception handling
                exception-middleware/wrap-exception]})

;; Routes
(defn api-routes [opts]
  [["/swagger.json"
    {:get {:no-doc  true
           :swagger {:info {:title "Utopia API"}}
           :handler (swagger/create-swagger-handler)}}]
   ["/health"
    {:get health/healthcheck!}]
   ["/cljs" (cljs/routes opts)]
   ["/examples" (examples/routes opts)]])

(derive :reitit.routes/api :reitit/routes)

(defmethod ig/init-key :reitit.routes/api
  [_ {:keys [env base-path]
      :or   {base-path ""}
      :as   opts}]
  (when (not
         (or (= env :test)
             (= env :debug)))
    (fn [] [base-path route-data (api-routes opts)])))
