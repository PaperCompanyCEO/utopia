(ns papercompany.utopia.web.handler
  (:require
    [papercompany.utopia.web.middlewares.core :as middlewares]
    [integrant.core :as ig]
    [ring.util.http-response :as http-response]
    [reitit.ring :as ring]
    [reitit.swagger-ui :as swagger-ui]))

(defmethod ig/init-key :handler/ring
  [_ {:keys [env router api-path] :as opts}]
  (when (not
         (or (= env :test)
             (= env :debug)))
    (ring/ring-handler
     (router)
     (ring/routes
      ;; Handle trailing slash in routes - add it + redirect to it
      ;; https://github.com/metosin/reitit/blob/master/doc/ring/slash_handler.md 
      (ring/redirect-trailing-slash-handler)
      (ring/create-resource-handler {:path "/"})
      (when (some? api-path)
        (swagger-ui/create-swagger-ui-handler {:path api-path
                                               :url  (str api-path "/swagger.json")}))
      (ring/create-default-handler
       {:not-found
        (constantly (-> {:status 404, :body "Page not found"}
                        (http-response/content-type "text/plain")))
        :method-not-allowed
        (constantly (-> {:status 405, :body "Not allowed"}
                        (http-response/content-type "text/plain")))
        :not-acceptable
        (constantly (-> {:status 406, :body "Not acceptable"}
                        (http-response/content-type "text/plain")))}))
     {:middleware [(middlewares/wrap-base opts)]})))

(defmethod ig/init-key :router/routes
  [_ {:keys [env routes]}]
  (when (not
         (or (= env :test)
             (= env :debug)))
    (mapv (fn [route]
            (if (fn? route)
              (route)
              route))
          routes)))

(defmethod ig/init-key :router/core
  [_ {:keys [env routes] :as opts}]
  (when (not
         (or (= env :test)
             (= env :debug)))
    (if (= env :dev)
      #(ring/router ["" opts routes])
      (constantly (ring/router
                   ["" opts routes])))))
