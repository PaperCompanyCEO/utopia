(ns papercompany.utopia.web.controllers.examples
  (:require
   [papercompany.utopia.web.services.examples :as examples-service]))

(require '[papercompany.utopia.specs.web.examples.requests.get-test :as examples-request-get-test])
(require '[papercompany.utopia.specs.web.examples.requests.post-test :as examples-request-post-test])

(defn get-test [with-utopia-transaction utopia-query-fn]
  (fn [{:keys [fx-cofx transactions] :as req}]
    (examples-service/get-test
     {:with-utopia-transaction with-utopia-transaction
      :utopia-query-fn utopia-query-fn}
     {:fx-cofx fx-cofx
      :transactions transactions}
     req)))

(defn post-test [with-utopia-transaction utopia-query-fn]
  (fn [{:keys [fx-cofx transactions] :as req}]
    (examples-service/post-test
     {:with-utopia-transaction with-utopia-transaction
      :utopia-query-fn utopia-query-fn}
     {:fx-cofx fx-cofx
      :transactions transactions}
     req)))

(defn routes [{:keys [with-utopia-transaction
                      utopia-query-fn]}]
  [["/test/:msg2" {:get {:handler (get-test with-utopia-transaction utopia-query-fn)
                         :parameters {:header examples-request-get-test/header-spec
                                      :path examples-request-get-test/path-spec
                                      :query examples-request-get-test/query-spec
                                      :body examples-request-get-test/body-spec}}
                   :post {:handler (post-test with-utopia-transaction utopia-query-fn)
                          :parameters {:header examples-request-post-test/header-spec
                                       :path examples-request-post-test/path-spec
                                       :query examples-request-post-test/query-spec
                                       :body examples-request-post-test/body-spec}}}]])
