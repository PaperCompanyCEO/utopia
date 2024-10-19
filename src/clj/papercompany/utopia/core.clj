(ns papercompany.utopia.core
  (:require
   [papercompany.utopia.integrant.state :as state]
   [papercompany.utopia.integrant.config :as config]
   [papercompany.utopia.env :refer [defaults]]
   
   [clojure.tools.logging :as log]
   [integrant.core :as ig]
   
   ;; Edges       
   [papercompany.utopia.ring.undertow]
   [papercompany.utopia.web.handler]

   ;; Routes
   [papercompany.utopia.web.routes.api])
  (:import
   [java.util TimeZone])
  (:gen-class))

(TimeZone/setDefault (TimeZone/getTimeZone "UTC"))

;; log uncaught exceptions in threads
(Thread/setDefaultUncaughtExceptionHandler
 (reify Thread$UncaughtExceptionHandler
   (uncaughtException [_ thread ex]
     (log/error {:what :uncaught-exception
                 :exception ex
                 :where (str "Uncaught exception on" (.getName thread))}))))

(defn stop-app []
  ((or (:stop defaults) (fn [])))
  (some-> (deref state/system) (ig/halt!)))

(defn start-app [& [params]]
  ((or (:start params) (:start defaults) (fn [])))
  (->> (config/system-config (or (:opts params) (:opts defaults) {}))
       (ig/expand)
       (ig/init)
       (reset! state/system)))

(defn -main [& _]
  (start-app)
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop-app))
  (.addShutdownHook (Runtime/getRuntime) (Thread. shutdown-agents)))
