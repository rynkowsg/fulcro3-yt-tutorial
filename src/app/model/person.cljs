(ns app.model.person
  (:require
   [com.fulcrologic.fulcro.mutations :refer [defmutation]]))

(defn person-path [& ks] (into [:person/id] ks))
(defn picker-path [k] [:component/id :person-picker k])
(defn person-list-path [k] [:component/id :person-list k])

(defmutation make-older [{:person/keys [id] :as params}]
  (action [{:keys [state]}]
    (swap! state update-in (person-path id :person/age) inc))
  (result-action [env]
    (js/console.log (keys env)))
  (remote [env] true))

(defmutation select-person [{:person/keys [id] :as params}]
  (action [{:keys [app state]}]
    (swap! state assoc-in (picker-path :person-picker/selected-person) [:person/id id])))
