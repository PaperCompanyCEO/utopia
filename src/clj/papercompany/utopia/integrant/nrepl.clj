(ns papercompany.utopia.integrant.nrepl
  (:require
   [papercompany.utopia.integrant.utils :as ig-utils]
   [clojure.tools.logging :as log]
   [integrant.core :as ig]
   [cider.nrepl :as cider]
   [nrepl.cmdline]
   [nrepl.server :as nrepl]))

(defmethod ig/init-key :nrepl/server
  [_ {:keys [port bind ack-port create-nrepl-port-file?] :as config}]
  (try
    (let [server (nrepl/start-server :port port
                                     :bind bind
                                     :ack-port ack-port
                                     :handler cider/cider-nrepl-handler)]
      (when create-nrepl-port-file?
        (nrepl.cmdline/save-port-file server {}))
      (log/info "nREPL server started on port:" port)
      (assoc config ::server server))
    (catch Exception e
      (log/error "failed to start the nREPL server on port:" port)
      (throw e))))

(defmethod ig/suspend-key! :nrepl/server [_ _])

(defmethod ig/halt-key! :nrepl/server
  [_ {::keys [server]}]
  (nrepl/stop-server server)
  (log/info "nREPL server stopped"))

(defmethod ig/resume-key :nrepl/server
  [key opts old-opts old-impl]
  (ig-utils/resume-handler key opts old-opts old-impl))
