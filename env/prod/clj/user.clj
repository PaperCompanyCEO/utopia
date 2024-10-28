(ns user
  (:require
   [papercompany.utopia.state :as state]))

(defn actions
  ([system name args]
   ((get (:papercompany.utopia/actions system) name) args))
  ([name args]
   (actions @state/system name args)))
