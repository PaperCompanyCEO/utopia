(ns papercompany.utopia.env
  (:require
    [clojure.tools.logging :as log]
    [papercompany.utopia.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init       (fn []
                 (log/info "\n-=[utopia starting using the development or test profile]=-"))
   :start      (fn []
                 (log/info "\n-=[utopia started successfully using the development or test profile]=-"))
   :stop       (fn []
                 (log/info "\n-=[utopia has shut down successfully]=-"))
   :middleware wrap-dev
   :opts       {:profile       :dev
                :persist-data? true}})
