(ns user)

(println "enter user.cljs")

(comment
 :cljs/quit
 (js/console.log {:msg "Console from log" :data {:keyword-key :keyword-value
                                                 "string key" "string value"}})
 (js/alert "Message from alert"))
