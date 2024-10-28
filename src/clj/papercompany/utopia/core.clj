(ns papercompany.utopia.core
  (:require
   [papercompany.utopia.specs.core :as specs]
   [papercompany.utopia.state :as state]
   [papercompany.utopia.integrant.config :as config]
   [papercompany.utopia.env :refer [defaults]]
   
   [clojure.tools.logging :as log]
   [integrant.core :as ig]
   [malli.registry :as malli-registry]

   ;; nREPL
   [papercompany.utopia.integrant.nrepl]
   
   ;; Edges       
   [papercompany.utopia.ring.undertow]
   [papercompany.utopia.web.handler]

   ;; Routes
   [papercompany.utopia.web.routes.pages]
   [papercompany.utopia.web.routes.api]

   ;; Effects
   [papercompany.utopia.effects.utopia-db]
   [papercompany.utopia.effects.dynamic-db]

   ;; Actions
   [papercompany.utopia.actions.core])
  (:import
   [java.util TimeZone])
  (:gen-class))

(TimeZone/setDefault (TimeZone/getTimeZone "UTC"))

(malli-registry/set-default-registry!
 (specs/registry))

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
