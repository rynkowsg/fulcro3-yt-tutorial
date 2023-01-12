(ns dev-client.shadow                                       ;; copy of shadow.user
  (:require
   [clojure.repl :refer (source apropos dir pst doc find-doc)]
   [clojure.java.javadoc :refer (javadoc)]
   [clojure.pprint :refer (pp pprint)]
   [shadow.cljs.devtools.api :as shadow :refer (help)]))

(println "enter clj")

(comment
 ;; Show shadow-cljs compiler env
 (System/getenv)
 ;; Open CLJS REPL
 (shadow/repl :main))

(defn dev []
  (println "opening cljs repl...")
  (shadow/repl :main))
