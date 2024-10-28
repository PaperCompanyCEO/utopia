(ns papercompany.utopia.web.middlewares.input)

(def input-middleware
  {:name ::input
   :wrap (fn [handler]
           (fn [{:keys [protocol
                        cookies
                        remote-addr
                        params
                        headers
                        server-port
                        query-string
                        path-params
                        body-params
                        form-params
                        multipart-params
                        request-method] :as request}]
             (handler (-> request
                          (assoc :input {:protocol protocol
                                         :cookies cookies
                                         :remote-addr remote-addr
                                         :params params
                                         :headers headers
                                         :server-port server-port
                                         :query-string query-string
                                         :path-params path-params
                                         :body-params body-params
                                         :form-params form-params
                                         :multipart-params multipart-params
                                         :request-method request-method})))))})
