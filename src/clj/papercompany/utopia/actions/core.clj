(ns papercompany.utopia.actions.core
  (:require
   [papercompany.utopia.actions.examples :as examples]
   [integrant.core :as ig]
   [clojure.tools.logging :as log]
   [clojure.pprint :as pprint]
   [clojure.stacktrace :as stacktrace]))

(defn action [name
              f]
  (fn [input]
    (let [fx-cofx (atom [])
          transactions (atom {:next-id 0
                              :map {}})]
      (try (f {:fx-cofx fx-cofx
               :transactions transactions}
              input)
           (catch Exception e
             (log/error
              (with-out-str
                (pprint/pprint
                 {:action name
                  :input input
                  :fx-cofx @fx-cofx
                  :message (.getMessage e)
                  :data (ex-data e)
                  :exception e}))))))))

(defmethod ig/init-key :papercompany.utopia/actions
  [_ {:keys [with-utopia-transaction
             utopia-query-fn
             with-dynamic-transaction
             dynamic-query]}]
  {:hello-world (action
                 "hello-world"
                 (partial
                  examples/hello-world
                  {:with-utopia-transaction with-utopia-transaction
                   :utopia-query-fn utopia-query-fn}))})
