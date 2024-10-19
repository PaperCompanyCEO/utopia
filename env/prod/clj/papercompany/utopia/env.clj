(ns papercompany.utopia.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init       (fn []
                 (log/info "\n-=[utopia starting]=-"))
   :start      (fn []
                 (log/info "\n-=[utopia started successfully]=-"))
   :stop       (fn []
                 (log/info "\n-=[utopia has shut down successfully]=-"))
   :middleware (fn [handler _] handler)
   :opts       {:profile :prod}})
