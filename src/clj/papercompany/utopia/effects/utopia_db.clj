(ns papercompany.utopia.effects.utopia-db
  (:require
   [papercompany.utopia.specs.utopia-query-fn.core :as utopia-query-fn-spec]
   [papercompany.utopia.integrant.utils :as ig-utils]
   [malli.generator :as malli-generator]
   [malli.error :as malli-error]
   [malli.core :as malli]
   [clojure.tools.logging :as log]
   [conman.core :as conman]
   [migratus.core :as migratus]
   [integrant.core :as ig]
   [next.jdbc :as jdbc]))

(defmethod ig/init-key :db.sql/utopia-connection
  [_ {:keys [env]
      :as pool-spec}]
  (if (= env :test)
    nil
    (conman/connect! (dissoc pool-spec :env))))

(defmethod ig/suspend-key! :db.sql/utopia-connection [_ _])

(defmethod ig/halt-key! :db.sql/utopia-connection
  [_ conn]
  (when conn
    (conman/disconnect! conn)))

(defmethod ig/resume-key :db.sql/utopia-connection
  [key opts old-opts old-impl]
  (ig-utils/resume-handler key opts old-opts old-impl))

(defn queries-dev [load-queries]
  (fn
    ([query params]
     (conman/query (load-queries) query params))
    ([conn query params & opts]
     (conman/query conn (load-queries) query params opts))))

(defn queries-prod [load-queries]
  (let [queries (load-queries)]
    (fn
      ([query params]
       (conman/query queries query params))
      ([conn query params & opts]
       (conman/query conn queries query params opts)))))

(defmethod ig/init-key :db.sql/utopia-query-fn
  [_ {:keys [conn options filename filenames env]
      :or   {options {}}}]
  (if (= env :test)
    nil
    (let [filenames (or filenames [filename])
          load-queries #(apply conman/bind-connection-map conn options filenames)]
      (with-meta
        (if (= env :dev)
          (queries-dev load-queries)
          (queries-prod load-queries))
        {:mtimes (mapv ig-utils/last-modified filenames)}))))

(defmethod ig/suspend-key! :db.sql/utopia-query-fn [_ _])

(defmethod ig/resume-key :db.sql/utopia-query-fn
  [k {:keys [filename filenames] :as opts} old-opts old-impl]
  (let [check-res (and (= opts old-opts)
                       (= (mapv ig-utils/last-modified (or filenames [filename]))
                          (:mtimes (meta old-impl))))]
    (log/info k "resume check. Same?" check-res)
    (if check-res
      old-impl
      (do (ig/halt-key! k old-impl)
          (ig/init-key k opts)))))

(defn with-utopia-transaction-push-fx-cofx [fx-cofx transactions tx f]
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
        :category :utopia-tx
        :name :open
        :result tx-id}))
    (try (let [output (f tx)]
           (swap!
            fx-cofx
            #(conj
              %
              {:type :fx
               :category :utopia-tx
               :name :commit
               :result tx-id}))
           output)
         (catch Exception e
           (swap!
            fx-cofx
            #(conj
              %
              {:type :fx
               :category :utopia-tx
               :name :rollback
               :result tx-id}))
           (throw e)))))

(defmethod ig/init-key :papercompany.utopia/with-utopia-transaction
  [_ {:keys [env conn]}]
  (if (= env :test)
    (fn [fx-cofx transactions f]
      (with-utopia-transaction-push-fx-cofx fx-cofx transactions (Object.) f))
    (fn [fx-cofx transactions f]
      (jdbc/with-transaction [tx conn]
        (with-utopia-transaction-push-fx-cofx fx-cofx transactions tx f)))))

(defn utopia-query-fn-push-fx-cofx [fx-cofx cofx-res transactions tx name args]
  (let [tx-id (when tx
                (get-in @transactions [:map tx]))]
    (swap!
     fx-cofx
     #(vec
       (concat
        %
        [{:type :cofx
          :category :utopia-query-fn
          :tx tx-id
          :name name
          :result cofx-res}
         {:type :fx
          :category :utopia-query-fn
          :tx tx-id
          :name name
          :result args}])))))

(defmethod ig/init-key :papercompany.utopia/utopia-query-fn
  [_ {:keys [utopia-query-fn env]}]
  (if (= env :test)
    (fn
      ([fx-cofx name args]
       (let [cofx-res (malli-generator/generate
                       ((name utopia-query-fn-spec/cofx-spec) args)
                       {:registry ((name utopia-query-fn-spec/registry) args)})]
         (utopia-query-fn-push-fx-cofx fx-cofx cofx-res nil nil name args)
         cofx-res))
      ([fx-cofx transactions tx name args]
       (let [cofx-res (malli-generator/generate
                       ((name utopia-query-fn-spec/cofx-spec) args)
                       {:registry ((name utopia-query-fn-spec/registry) args)})]
         (utopia-query-fn-push-fx-cofx fx-cofx cofx-res transactions tx name args)
         cofx-res)))
    (fn
      ([fx-cofx name args]
       (let [cofx-res (utopia-query-fn name args)]
         (when-let [cofx-error (malli/explain
                                ((name utopia-query-fn-spec/cofx-spec) args)
                                cofx-res
                                {:registry ((name utopia-query-fn-spec/registry) args)})]
           (swap!
            fx-cofx
            #(conj
              %
              {:type :cofx-error
               :category :utopia-query-fn
               :tx nil
               :name name
               :result {:value cofx-res
                        :error (malli-error/humanize cofx-error)}}))
           (throw (ex-info
                   "cofx-error"
                   {:type :papercompany/cofx})))
         (utopia-query-fn-push-fx-cofx fx-cofx cofx-res name args)
         cofx-res))
      ([fx-cofx transactions tx name args]
       (let [tx-id (get-in @transactions [:map tx])
             cofx-res (utopia-query-fn tx name args)]
         (when-let [cofx-error (malli/explain
                                ((name utopia-query-fn-spec/cofx-spec) args)
                                cofx-res
                                {:registry ((name utopia-query-fn-spec/registry) args)})]
           (swap!
            fx-cofx
            #(conj
              %
              {:type :cofx-error
               :category :utopia-query-fn
               :tx tx-id
               :name name
               :result {:value cofx-res
                        :error (malli-error/humanize cofx-error)}}))
           (throw (ex-info
                   "cofx-error"
                   {:type :papercompany/cofx
                    :cofx-error cofx-error})))
         (utopia-query-fn-push-fx-cofx fx-cofx cofx-res transactions tx name args)
         cofx-res)))))

(defmethod ig/init-key :db.sql/utopia-migrations
  [_ {:keys [env migrate-on-init?]
      :or   {migrate-on-init? true}
      :as   component}]
  (when (and (not (= env :test))
             migrate-on-init?)
    (migratus/migrate component))
  component)
