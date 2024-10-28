(ns papercompany.utopia.events.core
  (:require
   [papercompany.utopia.doms.main.home :as home]
   [re-frame.alpha :as re-frame-alpha]))

(re-frame-alpha/reg-event-fx
 :initialise-db
 []
 (fn [{:keys [db]} _]
   {:db {:pages {:main home/home-page}}}))

(re-frame-alpha/reg-event-fx
 :push-state
 []
 (fn [{:keys [db]} [_ dir page]]
   {:db (assoc-in db
                  [:pages dir]
                  page)}))

(re-frame-alpha/reg-event-fx
 :route
 []
 (fn [{} [_ name path-params query-params]]
   {:route [name path-params query-params]}))

(re-frame-alpha/reg-event-fx
 :test
 [(re-frame-alpha/inject-cofx :env)]
 (fn [{:keys [env]} [_]]
   {:alert env
    :test-alert nil}))
