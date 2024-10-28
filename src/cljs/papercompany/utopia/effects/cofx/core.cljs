(ns papercompany.utopia.effects.cofx.core
  (:require
   [integrant.core :as ig]
   [re-frame.alpha :as re-frame-alpha]))

(defmethod ig/init-key :cofx/env
  [_ {:keys [env]}]
  (re-frame-alpha/reg-cofx
   :env
   (fn [cofx _]
     (assoc cofx :env env))))
