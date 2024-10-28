(ns papercompany.utopia.web.services.cljs
  (:require
   [aero.core :as aero]
   [clojure.java.io :as io]))

(defn system-config
  [{}
   {:keys [fx-cofx]}
   {{{:keys [profile]} :query} :parameters}]
  (let [system-config
        (aero/read-config
         (io/resource "cljs/system.edn")
         {:profile (keyword profile)
          :persist-data? true})]
    {:status 200
     :headedrs {}
     :body (pr-str system-config)}))

