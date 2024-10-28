(ns papercompany.utopia.subscriptions.core
  (:require
   [re-frame.alpha :as re-frame-alpha]))

(re-frame-alpha/reg-sub
 :page
 (fn [db [_ dir]]
   (get-in db [:pages dir])))
