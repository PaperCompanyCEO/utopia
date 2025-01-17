(ns user
  "Userspace functions you can run by default in your local REPL."
  (:require
   [papercompany.utopia.integrant.config :as config]
   [clojure.pprint]
   [clojure.spec.alpha :as s]
   [clojure.tools.namespace.repl :as repl]
   [criterium.core :as c]                                  ;; benchmarking
   [expound.alpha :as expound]
   [integrant.core :as ig]
   [integrant.repl :refer [clear go halt prep init reset reset-all]]
   [integrant.repl.state :as state]
   [lambdaisland.classpath.watch-deps :as watch-deps]      ;; hot loading for deps
   [migratus.core :as migratus]
   [papercompany.utopia.core :refer [start-app]]))

;; uncomment to enable hot loading for deps
(watch-deps/start! {:aliases [:dev :test]})

(alter-var-root #'s/*explain-out* (constantly expound/printer))

(add-tap (bound-fn* clojure.pprint/pprint))

(defn dev-prep!
  []
  (integrant.repl/set-prep! (fn []
                              (-> (papercompany.utopia.integrant.config/system-config {:profile :dev})
                                  (ig/expand)))))

(defn test-prep!
  []
  (integrant.repl/set-prep! (fn []
                              (-> (papercompany.utopia.integrant.config/system-config {:profile :test})
                                  (ig/expand)))))

;; Can change this to test-prep! if want to run tests as the test profile in your repl
;; You can run tests in the dev profile, too, but there are some differences between
;; the two profiles.
(dev-prep!)

(repl/set-refresh-dirs "src/clj")

(def refresh repl/refresh)

(defn migratus []
  (:db.sql/utopia-migrations state/system))

(defn actions
  ([system name args]
   ((get (:papercompany.utopia/actions system) name) args))
  ([name args]
   (actions state/system name args)))

(defonce dbg-sys (atom nil))

(defn stop-dbg []
  (some-> (deref dbg-sys)
          (ig/halt!)))

(defn start-dbg []
  (stop-dbg)
  (->> (config/system-config {:profile :debug
                              :persist-data? true})
       (ig/expand)
       (ig/init)
       (reset! dbg-sys))
  nil)
