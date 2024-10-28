(ns papercompany.utopia.web.middlewares.transactions)

(def transactions-middleware
  {:name ::transactions
   :wrap (fn [handler]
           (fn [request]
             (handler (assoc request :transactions (atom {:next-id 0
                                                          :map {}})))))})
