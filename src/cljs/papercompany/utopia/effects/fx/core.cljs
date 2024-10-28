(ns papercompany.utopia.effects.fx.core
  (:require
   [papercompany.utopia.state :as state]
   [integrant.core :as ig]
   [reitit.frontend.history :as rfh]
   [re-frame.alpha :as re-frame-alpha]))

(re-frame-alpha/reg-fx
 :route
 (fn [[name path-params query-params]]
   (rfh/push-state @state/history name path-params query-params)))

(re-frame-alpha/reg-fx
 :alert
 (fn [x]
   (js/alert (str x))))

(defmethod ig/init-key :fx/test-alert
  [_ {:keys [env]}]
  (re-frame-alpha/reg-fx
   :test-alert
   (fn []
     (js/alert (case env
                 :prod
                 "반짝핑"
                 :dev
                 "또너핑"
                 :test
                 "아아핑"
                 :debug
                 "하츄핑")))))
