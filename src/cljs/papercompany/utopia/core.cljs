(ns papercompany.utopia.core
    (:require
     [papercompany.utopia.doms.main-page :as main-page]
     [papercompany.utopia.events.core]
     [papercompany.utopia.subscriptions.core]
     [papercompany.utopia.reitit.core]
     [papercompany.utopia.integrant.config :as config]
     [papercompany.utopia.state :as state]
     [reitit.frontend.history :as rfh]
     [reagent.dom :as reagent-dom]
     [re-frame.alpha :as re-frame-alpha]
     [re-frame.core :as re-frame]
     [integrant.core :as ig]
     [clojure.core.async :as async]))

(set! *warn-on-infer* false)

(defn init! [profile]
  (async/go
    (let [system-chan (async/chan)]
      (config/system-config system-chan {:profile profile})
      (let [{:keys [error?
                    res
                    config]} (async/<! system-chan)]
        (if error?
          (js/alert "system.edn 가져오기 실패")
          (do (reset! state/system
                      (->> config
                           (ig/expand)
                           (ig/init)))
              (reset! state/history
                      (rfh/start! (:router/core @state/system)
                                  (fn [{:keys [data
                                               parameters]}]
                                    (re-frame/dispatch [:push-state (:dir data) ((:fn data) data parameters)]))
                                  {:use-fragment false}))
              (re-frame-alpha/dispatch-sync [:initialise-db profile])
              (reagent-dom/render [main-page/main-page profile] (.getElementById js/document "app"))))))))

(defn init!-prod []
  (init! :prod))

(defn init!-dev []
  (init! :dev))
