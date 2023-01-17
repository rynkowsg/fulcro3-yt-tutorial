(ns server.dev
  (:require
   [app.server :refer [middleware]]
   [clojure.tools.namespace.repl :as tools-ns]
   [org.httpkit.server :as http]
   [taoensso.timbre :as log]))

(println "enter server.dev.clj")

(tools-ns/set-refresh-dirs "src" "dev")

(defonce server (atom nil))

(defn start []
  (let [port   3000
        result (http/run-server middleware {:port port})]
    (log/info "Started web server on port" port)
    (reset! server result)
    :ok))

(defn stop []
  (when @server
    (log/info "Stopped web server")
    (@server)))

(defn restart []
  (stop)
  (log/info "Reloadinng code")
  (tools-ns/refresh :after 'server.dev/start))

(comment
 (start)
 (stop)
 (restart)

 (do (stop)
     (tools-ns/refresh-all)))
