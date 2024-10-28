(ns papercompany.utopia.specs.web.cljs.services.system-config
  (:require
   [papercompany.utopia.specs.web.cljs.requests.system-config :as request]
   [papercompany.utopia.specs.core :as specs]))

(def registry
  (specs/registry))

(def dom-spec
  [:map
   [:parameters
    [:map
     [:query request/query-spec]]]])

(defn codom-spec [input fx-cofx]
  [:any])

(defn fx-cofx-spec [input fx-cofx]
  [:tuple])
