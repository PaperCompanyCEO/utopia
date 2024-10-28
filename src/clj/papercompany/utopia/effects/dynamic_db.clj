(ns papercompany.utopia.effects.dynamic-db
  (:require
   [malli.generator :as malli-generator]
   [malli.error :as malli-error]
   [malli.core :as malli]
   [integrant.core :as ig]
   [next.jdbc :as jdbc]
   [clojure.edn :as edn]))

(defn with-dynamic-transaction-push-fx-cofx [db-name fx-cofx transactions tx f]
  (let [tx-id-atom (atom nil)
        _ (swap!
           transactions
           #(do
              (reset! tx-id-atom (:next-id %))
              {:next-id (inc (:next-id %))
               :map (assoc
                     (:map tx)
                     tx
                     (:next-id %))}))
        tx-id @tx-id-atom]
    (swap!
     fx-cofx
     #(conj
       %
       {:type :fx
        :category (keyword (str (name db-name) "-tx"))
        :name :open
        :result tx-id}))
    (try (let [output (f tx)]
           (swap!
            fx-cofx
            #(conj
              %
              {:type :fx
               :category (keyword (str (name db-name) "-tx"))
               :name :commit
               :result tx-id}))
           output)
         (catch Exception e
           (swap!
            fx-cofx
            #(conj
              %
              {:type :fx
               :category (keyword (str (name db-name) "-tx"))
               :name :rollback
               :result tx-id}))
           (throw e)))))

(defmethod ig/init-key :papercompany.utopia/with-dynamic-transaction
  [_ {:keys [env]}]
  (if (or
       (= env :test)
       (= env :debug))
    (fn [db-name _db-config fx-cofx transactions f]
      (with-dynamic-transaction-push-fx-cofx db-name fx-cofx transactions (Object.) f))
    (fn [db-name db-config fx-cofx transactions f]
      (jdbc/with-transaction [tx (jdbc/get-datasource db-config)]
        (with-dynamic-transaction-push-fx-cofx db-name fx-cofx transactions tx f)))))

(defn dynamic-query-push-fx-cofx [db-name fx-cofx cofx-res transactions tx sql-args]
  (let [tx-id (when tx
                (get-in @transactions [:map tx]))]
    (swap!
     fx-cofx
     #(vec
       (concat
        %
        [{:type :cofx
          :category :dynamic-db
          :tx tx-id
          :name db-name
          :result cofx-res}
         {:type :fxp
          :category :dynamic-db
          :tx tx-id
          :name db-name
          :result sql-args}])))))

(defn dynamic-query [{:keys [db-config tx sql-args]}]
  (if tx
    (jdbc/execute! tx sql-args)
    (let [datasource (jdbc/get-datasource db-config)]
      (with-open [connection (jdbc/get-connection datasource)]
        (jdbc/execute! connection sql-args)))))

(defmethod ig/init-key :papercompany.utopia/dynamic-query
  [_ {:keys [env]}]
  (case env
    :test
    (fn
      ([db-name fx-cofx _db-config sql-args spec registry]
       (let [cofx-res (malli-generator/generate
                       spec
                       {:registry registry})]
         (dynamic-query-push-fx-cofx db-name fx-cofx cofx-res nil nil sql-args)
         cofx-res))
      ([db-name fx-cofx transactions tx sql-args spec registry]
       (let [cofx-res (malli-generator/generate
                       spec
                       {:registry registry})]
         (dynamic-query-push-fx-cofx db-name fx-cofx cofx-res transactions tx sql-args)
         cofx-res)))
    :debug
    (fn
      ([db-name fx-cofx _db-config sql-args spec registry]
       (let [cofx-file-name (str "dbg-inputs/clj/" (read-line))
             cofx-file-content (slurp cofx-file-name)
             cofx-res (edn/read-string cofx-file-content)]
         (when-let [cofx-error (malli/explain
                                spec
                                cofx-res
                                {:registry registry})]
           (swap!
            fx-cofx
            #(conj
              %
              {:type :cofx-error
               :category :dynamic-db
               :tx nil
               :name db-name
               :result {:value cofx-res
                        :error (malli-error/humanize cofx-error)}}))
           (throw (ex-info
                   "cofx-error"
                   {:type :papercompany/cofx})))
         (dynamic-query-push-fx-cofx db-name fx-cofx cofx-res nil nil sql-args)
         cofx-res))
      ([db-name fx-cofx transactions tx sql-args spec registry]
       (let [cofx-file-name (str "dbg-inputs/clj/" (read-line))
             cofx-file-content (slurp cofx-file-name)
             cofx-res (edn/read-string cofx-file-content)]
         (when-let [cofx-error (malli/explain
                                spec
                                cofx-res
                                {:registry registry})]
           (swap!
            fx-cofx
            #(conj
              %
              {:type :cofx-error
               :category :dynamic-db
               :tx nil
               :name db-name
               :result {:value cofx-res
                        :error (malli-error/humanize cofx-error)}}))
           (throw (ex-info
                   "cofx-error"
                   {:type :papercompany/cofx})))
         (dynamic-query-push-fx-cofx db-name fx-cofx cofx-res transactions tx sql-args)
         cofx-res)))
    (fn
      ([db-name fx-cofx db-config sql-args spec registry]
       (let [cofx-res (dynamic-query {:db-config db-config
                                      :sql-args sql-args})]
         (when-let [cofx-error (malli/explain
                                spec
                                cofx-res
                                {:registry registry})]
           (swap!
            fx-cofx
            #(conj
              %
              {:type :cofx-error
               :category :dynamic-db
               :tx nil
               :name db-name
               :result {:value cofx-res
                        :error (malli-error/humanize cofx-error)}}))
           (throw (ex-info
                   "cofx-error"
                   {:type :papercompany/cofx})))
         (dynamic-query-push-fx-cofx db-name fx-cofx cofx-res nil nil sql-args)
         cofx-res))
      ([db-name fx-cofx transactions tx sql-args spec registry]
       (let [tx-id (get-in @transactions [:map tx])
             cofx-res (dynamic-query {:tx tx
                                      :sql-args sql-args})]
         (when-let [cofx-error (malli/explain
                                spec
                                cofx-res
                                {:registry registry})]
           (swap!
            fx-cofx
            #(conj
              %
              {:type :cofx-error
               :category :dynamic-db
               :tx tx-id
               :name db-name
               :result {:value cofx-res
                        :error (malli-error/humanize cofx-error)}}))
           (throw (ex-info
                   "cofx-error"
                   {:type :papercompany/cofx})))
         (dynamic-query-push-fx-cofx db-name fx-cofx cofx-res transactions tx sql-args)
         cofx-res)))))
