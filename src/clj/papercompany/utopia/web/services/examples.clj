(ns papercompany.utopia.web.services.examples)

(defn get-test
  [{:keys [with-utopia-transaction
           utopia-query-fn]}
   {:keys [fx-cofx
           transactions]}
   {{{:keys [msg1]} :header
     {:keys [msg2]} :path
     {:keys [msg3]} :query
     {:keys [msg4]} :body} :parameters}]
  {:status 200
   :headers {"test" "Hello, World!"}
   :body {:msg1 msg1
          :msg2 msg2
          :msg3 msg3
          :msg4 msg4}})

(defn post-test
  [{:keys [with-utopia-transaction
           utopia-query-fn]}
   {:keys [fx-cofx
           transactions]}
   {{{:keys [msg1]} :header
     {:keys [msg2]} :path
     {:keys [msg3]} :query
     {:keys [msg4]} :body} :parameters}]
  {:status 200
   :headers {"test" "Hello, World!"}
   :body {:msg1 msg1
          :msg2 msg2
          :msg3 msg3
          :msg4 msg4}})
