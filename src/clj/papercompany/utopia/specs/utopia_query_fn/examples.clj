(ns papercompany.utopia.specs.utopia-query-fn.examples
  (:require
   [papercompany.utopia.specs.core :as specs]))

(defn get-examples-registry [_args]
  (specs/registry))

(defn get-examples-cofx-spec [_args]
  [:vector
   [:map
    [:id :int]
    [:name :string]]])
