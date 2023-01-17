(ns app.client
  (:require
   [com.fulcrologic.fulcro.algorithms.react-interop :as interop]
   [app.model.person :refer [make-older]]
   [com.fulcrologic.fulcro.application :as app]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.data-fetch :as df]
   [app.specs.person :as person]
   [app.specs.car :as car]
   [com.fulcrologic.fulcro.dom :as dom :refer [button div h3 label ul]]
   [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
   [com.fulcrologic.fulcro.networking.http-remote :as http]))

(defsc Car [this {::car/keys [id model]}]
  {:query [::car/id ::car/model]
   :ident ::car/id}
  (js/console.log "Render car" id)
  (div {} "Model: " model))

(def ui-car (comp/factory Car {:keyfn ::car/id}))

(defsc Person [this {::person/keys [id name age cars] :as props}]
  {:query [::person/id ::person/name ::person/age {::person/cars (comp/get-query Car)}]
   :ident ::person/id}
  (js/console.log "Render person" id)
  (let [onClick (comp/get-state this :onClick)]
    (div :.ui.segment {}
      (div :.ui.form {}
        (div :.field {}
          (label {} "Name: ")
          name)
        (div :.field {}
          (label {} "Age: ")
          age))
      (button {:onClick #(do (comp/transact! this [(make-older {::person/id id})] {:refresh [:person-list/people]})
                             (js/console.log "Made" name "older from inline function"))}
        "Make older")
      (h3 {} "Cars:")
      (ul {}
        (map ui-car cars)))))

(def ui-person (comp/factory Person {:keyfn ::person/id}))

;; We have only one PersonList in the app, therefore we don't need an id for the list.
(defsc PersonList [this {:person-list/keys [people] :as props}]
  {:query         [{:person-list/people (comp/get-query Person)}]
   :ident         (fn [_ _] [:component/id ::person-list])
   :initial-state {:person-list/people []}}
  (js/console.log "Render person list")
  (let [cnt (reduce #(if (> (::person/age %2) 30) (inc %1) %1) 0 people)]
    (div :.ui.segment {}
      (h3 :.ui.header "People")
      (div {} "Over 30: " cnt)
      (ul {}
        (map ui-person people)))))


(def ui-person-list (comp/factory PersonList))

(defsc Root [this {:root/keys [list] :as props}]
  {:query         [{:root/list (comp/get-query PersonList)}]
   :initial-state {:root/list {}}}
  (js/console.log "Render root")
  (div {}
    (h3 {} "Application")
    (ui-person-list list)))

(defonce APP (app/fulcro-app {:remotes          {:remote (http/fulcro-http-remote {:url "/api"})}
                              :client-did-mount (fn [app]
                                                  (df/load! app :all-people Person ;; equivalent of {:all-people (comp/get-query Person}
                                                            {:target [:component/id ::person-list :person-list/people]}))}))

(defn ^:export init []
  (app/mount! APP Root "app")
  (js/console.log "Loaded"))

(defn ^:export refresh []
  (app/mount! APP Root "app")
  (js/console.log "Hot reload"))
