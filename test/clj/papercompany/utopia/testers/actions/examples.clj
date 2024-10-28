(ns papercompany.utopia.testers.actions.examples
  (:require
   [papercompany.utopia.specs.actions.examples.hello-world :as hello-world-s]
   [papercompany.utopia.actions.examples :as hello-world-f]
   [papercompany.utopia.test-utils :as test-utils]))

(defn hello-world-tester [{:papercompany.utopia/keys [with-utopia-transaction
                                                      utopia-query-fn]}
                          writer]
  (test-utils/tester
   (let [fx-cofx (atom nil)
         transactions (atom nil)]
     {:f (partial hello-world-f/hello-world
                  {:with-utopia-transaction with-utopia-transaction
                   :utopia-query-fn utopia-query-fn}
                  {:fx-cofx fx-cofx
                   :transactions transactions})
      :name "hello-world"
      :dom-spec hello-world-s/dom-spec
      :codom-spec hello-world-s/codom-spec
      :fx-cofx-spec hello-world-s/fx-cofx-spec
      :registry hello-world-s/registry
      :fx-cofx fx-cofx
      :transactions transactions
      :writer writer})))

(defn testers [system writer]
  [(hello-world-tester system writer)])
