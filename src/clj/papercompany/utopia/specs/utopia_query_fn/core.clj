(ns papercompany.utopia.specs.utopia-query-fn.core
  (:require
   [papercompany.utopia.specs.utopia-query-fn.examples :as examples]))

(def registry
  {:get-examples examples/get-examples-registry})

(def cofx-spec
  {:get-examples examples/get-examples-cofx-spec})
