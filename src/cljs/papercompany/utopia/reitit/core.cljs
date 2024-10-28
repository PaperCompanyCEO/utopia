(ns papercompany.utopia.reitit.core
  (:require
   [papercompany.utopia.reitit.routes.main]
   [reitit.frontend :as reitit-frontend]
   [reitit.coercion :as reitit-coercion]
   [integrant.core :as ig]))

(defmethod ig/init-key :router/core
  [_ {:keys [env routes]}]
  (reitit-frontend/router ["" {:env env} routes]
                          {:compile reitit-coercion/compile-request-coercers}))
