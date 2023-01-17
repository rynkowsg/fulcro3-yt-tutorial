(ns app.model.person
  (:require
   [app.specs.person :as person]
   [com.fulcrologic.fulcro.mutations :refer [defmutation]]))

(defmutation make-older [{::person/keys [id] :as params}]
  (action [{:keys [state]}]
    (do
      (js/console.log  "make-older - action, params: " params)
      (js/console.log "mutation make-older")
      (swap! state update-in [::person/id id ::person/age] inc)))
  (remote [env]
    (do
      (js/console.log "make-older - remote, env: " env)
      true)))
