(ns papercompany.utopia.actions.examples)

(defn hello-world
  [{:keys [with-utopia-transaction
           utopia-query-fn]}
   {:keys [fx-cofx
           transactions]}
   {:keys [intro]}]
  (with-utopia-transaction
    fx-cofx
    transactions
    (fn [tx]
      (println intro)
      (println (utopia-query-fn fx-cofx transactions tx
                                :get-examples {})))))
