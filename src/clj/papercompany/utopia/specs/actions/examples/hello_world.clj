(ns papercompany.utopia.specs.actions.examples.hello-world
  (:require
   [papercompany.utopia.specs.core :as specs]))

(def registry
  (specs/registry))

(def dom-spec
  [:map
   [:intro :string]])

(defn codom-spec [_input _fx-cofx]
  [:const {} nil])

(defn fx-cofx-spec [_input _fx-cofx]
  [:tuple
   [:const {}
    {:type :fx
     :category :utopia-tx
     :name :open
     :result 0}]
   [:map
    [:type [:const :cofx]]
    [:category [:const :utopia-query-fn]]
    [:tx [:const 0]]
    [:name [:const :get-examples]]
    [:result
     [:vector
      [:map
       [:id :int]
       [:name :string]]]]]
   [:const {}
    {:type :fx
     :category :utopia-query-fn
     :tx 0
     :name :get-examples
     :result {}}]
   [:const {}
    {:type :fx
     :category :utopia-tx
     :name :commit
     :result 0}]])
