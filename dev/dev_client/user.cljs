(ns dev-client.user)                                        ;; copy of shadow.user

(println "enter cljs")

(comment
 :cljs/quit
 (js/console.log {:msg "Console from log" :data {:a :A}})
 (js/alert "Message from alert"))
