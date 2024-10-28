(ns papercompany.utopia.core-test
  (:require
   [papercompany.utopia.testers.services.examples :as examples-service]
   [papercompany.utopia.testers.actions.examples :as examples-action]
   [papercompany.utopia.test-utils :as test-utils]
   [papercompany.utopia.integrant.config :as config]
   [papercompany.utopia.core]
   [integrant.core :as ig]
   [clojure.java.io :as io]))

(defn main [_]
  (let [test-sys (->> (config/system-config {:profile :test
                                             :persist-data? true})
                      (ig/expand)
                      (ig/init))]
    (with-open [writer (io/writer "out/clj/test-results.org")]
      (.write writer "#+OPTIONS: ^:{} H:0 num:0\n\n")
      (test-utils/domain-test {:name "[Service] Examples"
                               :testers (examples-service/testers test-sys writer)
                               :writer writer})
      (test-utils/domain-test {:name "[Actions] Examples"
                              :testers (examples-action/testers test-sys writer)
                              :writer writer}))
    (ig/halt! test-sys)))
