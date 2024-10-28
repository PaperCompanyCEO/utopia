(ns papercompany.utopia.core-test
    (:require
     [papercompany.utopia.core :as core]
     [flow-storm.preload]))

(defn init!-test []
  (core/init! :test))

(defn init!-debug []
  (core/init! :debug))
