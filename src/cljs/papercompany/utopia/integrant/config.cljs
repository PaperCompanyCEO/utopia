(ns papercompany.utopia.integrant.config
  (:require
   [ajax.core :as ajax]
   [integrant.core :as ig]
   [clojure.core.async :as async]
   [cljs.reader :as reader]))

(defmethod ig/init-key :system/env
  [_ env]
  env)

(reader/register-tag-parser! 'integrant.core.Ref ig/map->Ref)
(reader/register-tag-parser! 'integrant.core.RefSet ig/map->RefSet)

(defn system-config [chan {:keys [profile]}]
  (ajax/GET (str
             "/api/cljs/system-config?profile="
             (name profile))
            {:with-credentials true
             :handler (fn [res]
                        (async/go
                          (async/>! chan {:error? false
                                          :res res
                                          :config (reader/read-string res)})))
             :error-handler (fn [e]
                              (async/go
                                (async/>! chan {:error? true
                                                :res e})))}))
