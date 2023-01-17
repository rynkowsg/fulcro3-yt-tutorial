(ns user
  (:require
   [clojure.tools.namespace.repl :as tools-ns]
   [shadow.cljs.devtools.api :as shadow]))

(println "enter user.clj")

(def shadow? (contains? (System/getenv) "SHADOW_CLI_PID"))

(defn client-dev []
  (println "opening cljs repl...")
  (shadow/repl :main))

(defn server-dev []
  "Load and switch to the 'dev' namespace."
  []
  (tools-ns/set-refresh-dirs "dev" "src")
  (tools-ns/refresh)
  (in-ns 'server.dev)
  :loaded)

(defn dev []
  (if shadow? (client-dev) (server-dev)))

#_ (dev)
