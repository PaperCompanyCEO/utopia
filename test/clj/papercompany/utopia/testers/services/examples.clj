(ns papercompany.utopia.testers.services.examples
  (:require
   [papercompany.utopia.specs.web.examples.services.get-test :as get-test-s]
   [papercompany.utopia.specs.web.examples.services.post-test :as post-test-s]
   [papercompany.utopia.web.services.examples :as examples-f]
   [papercompany.utopia.test-utils :as test-utils]))

(defn get-test-tester [{:papercompany.utopia/keys [with-utopia-transaction
                                                   utopia-query-fn]}
                       writer]
  (test-utils/tester
   (let [fx-cofx (atom nil)
         transactions (atom nil)]
     {:f (partial examples-f/get-test
                  {:with-utopia-transaction with-utopia-transaction
                   :utopia-query-fn utopia-query-fn}
                  {:fx-cofx fx-cofx
                   :transactions transactions})
      :name "get-test"
      :dom-spec get-test-s/dom-spec
      :codom-spec get-test-s/codom-spec
      :fx-cofx-spec get-test-s/fx-cofx-spec
      :registry get-test-s/registry
      :fx-cofx fx-cofx
      :transactions transactions
      :writer writer})))

(defn post-test-tester [{:papercompany.utopia/keys [with-utopia-transaction
                                                    utopia-query-fn]}
                        writer]
  (test-utils/tester
   (let [fx-cofx (atom nil)
         transactions (atom nil)]
     {:f (partial examples-f/post-test
                  {:with-utopia-transaction with-utopia-transaction
                   :utopia-query-fn utopia-query-fn}
                  {:fx-cofx fx-cofx
                   :transactions transactions})
      :name "post-test"
      :dom-spec post-test-s/dom-spec
      :codom-spec post-test-s/codom-spec
      :fx-cofx-spec post-test-s/fx-cofx-spec
      :registry post-test-s/registry
      :fx-cofx fx-cofx
      :transactions transactions
      :writer writer})))

(defn testers [system writer]
  [(get-test-tester system writer)
   (post-test-tester system writer)])
