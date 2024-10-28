(ns papercompany.utopia.web.middlewares.fx-cofx)

(def fx-cofx-middleware
  {:name ::fx-cofx
   :wrap (fn [handler]
           (fn [request]
             (handler (assoc request :fx-cofx (atom [])))))})
