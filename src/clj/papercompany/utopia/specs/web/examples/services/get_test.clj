(ns papercompany.utopia.specs.web.examples.services.get-test
  (:require
   [papercompany.utopia.specs.web.examples.requests.get-test :as request]
   [papercompany.utopia.specs.core :as specs]))

(def registry
  (specs/registry))

(def dom-spec
  [:map
   [:parameters
    [:map
     [:header request/header-spec]
     [:path request/path-spec]
     [:query request/query-spec]
     [:body request/body-spec]]]])

(defn codom-spec [{{{:keys [msg1]} :header
                    {:keys [msg2]} :path
                    {:keys [msg3]} :query
                    {:keys [msg4]} :body} :parameters} fx-cofx]
  [:map
   [:status [:const {} 200]]
   [:headers [:map
              ["test" [:const {} "Hello, World!"]]]]
   [:body [:map
           [:msg1 [:const {} msg1]]
           [:msg2 [:const {} msg2]]
           [:msg3 [:const {} msg3]]
           [:msg4 [:const {} msg4]]]]])

(defn fx-cofx-spec [input fx-cofx]
  [:tuple])
