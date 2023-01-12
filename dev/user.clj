(ns user
  (:require
   [shadow.cljs.devtools.api :as shadow]))

(println "enter user.clj")

(defn dev []
  (println "opening cljs repl...")
  (shadow/repl :main))

#_ (dev)
