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
 (fn [{:keys [db]
       :as cofx} [_ dir page]]
   (assoc-in cofx
             [:db :pages dir]
             page)))
