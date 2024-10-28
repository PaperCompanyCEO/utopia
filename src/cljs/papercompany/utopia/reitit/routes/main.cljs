(ns papercompany.utopia.reitit.routes.main
  (:require
   [papercompany.utopia.doms.main.home :as home]
   [papercompany.utopia.doms.main.example :as example]
   [integrant.core :as ig]
   [reitit.coercion.malli :as malli]))

(def route-data
  {:coercion malli/coercion})

(defn main-routes [opts]
  [["/" {:name "/"
         :dir :main
         :fn home/home-page
         :parameters {}}]
   ["/example" {:name "/example"
                :dir :main
                :fn example/example-page
                :parameters {:query [:map
                                     [:query1 :int]]}}]])

(derive :reitit.routes/main :reitit/routes)

(defmethod ig/init-key :reitit.routes/main
  [_ {:keys [env base-path] :as opts}]
  [base-path route-data (main-routes opts)])
