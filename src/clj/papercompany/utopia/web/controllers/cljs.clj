(ns papercompany.utopia.web.controllers.cljs
  (:require
   [papercompany.utopia.web.services.cljs :as cljs-service]))

(require '[papercompany.utopia.specs.web.cljs.requests.system-config :as cljs-request-system-config])

(defn system-config []
  (fn [{:keys [fx-cofx] :as req}]
    (cljs-service/system-config
     {}
     {:fx-cofx fx-cofx}
     req)))

(defn routes [{:keys []}]
  [["/system-config" {:get {:handler (system-config)
                            :parameters {:query cljs-request-system-config/query-spec}}}]])
