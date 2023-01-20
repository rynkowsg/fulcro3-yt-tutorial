(ns app.client
  (:require
   [app.model.person :refer [make-older select-person]]
   [com.fulcrologic.fulcro.application :as app]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.dom :as dom :refer [a button div h3 label li ul]]
   [com.fulcrologic.fulcro.networking.http-remote :as http]))

(defsc Car [this {:car/keys [id model]}]
  {:query [:car/id :car/model]
   :ident :car/id}
  (js/console.log "Render car" id)
  (div {} "Model: " model))

(def ui-car (comp/factory Car {:keyfn :car/id}))

(defsc PersonDetail [this {:person/keys [id name age cars] :as props}]
  {:query [:person/id :person/name :person/age {:person/cars (comp/get-query Car)}]
   :ident :person/id}
  (js/console.log "Render PersonDetail" id)
  (let [onClick (comp/get-state this :onClick)]
    (div :.ui.segment
      (h3 :.ui.header "Selected Person")
      (when id
        (div :.ui.form
          (div :.field
            (label {:onClick onClick} "Name: ")
            name)
          (div :.field
            (label "Age: ")
            age)
          (button :.ui.button {:onClick (fn []
                                          (comp/transact! this
                                            [(make-older {:person/id id})]
                                            {:refresh [:person-list/people]}))}
            "Make Older")
          (h3 {} "Cars")
          (ul {}
            (map ui-car cars)))))))

(def ui-person-detail (comp/factory PersonDetail {:keyfn :person/id}))

(defsc PersonListItem [this {:person/keys [id name]}]
  {:query [:person/id :person/name]
   :ident :person/id}
  (js/console.log "Render PersonListItem" id)
  (li :.item
      (a {:href    "#"
          :onClick (fn []
                     (comp/transact! this [(select-person {:person/id id})]))}
         name)))

(def ui-person-list-item (comp/factory PersonListItem {:keyfn :person/id}))

;; We have only one PersonList in the app, therefore we don't need an id for the list.
(defsc PersonList [this {:person-list/keys [people] :as props}]
  {:query         [{:person-list/people (comp/get-query PersonListItem)}]
   :ident         (fn [_ _] [:component/id :person-list])
   :initial-state {:person-list/people []}}
  (js/console.log "Render PersonList")
  (div :.ui.segment {}
    (h3 :.ui.header "People")
    (ul
      (map ui-person-list-item people))))


(def ui-person-list (comp/factory PersonList))

(defsc PersonPicker [this {:person-picker/keys [list selected-person]}]
  {:query         [{:person-picker/list (comp/get-query PersonList)}
                   {:person-picker/selected-person (comp/get-query PersonDetail)}]
   :initial-state {:person-picker/list {}}
   :ident         (fn [] [:component/id :person-picker])}
  (js/console.log "Render PersonPicker")
  (div :.ui.two.column.container.grid
    (div :.column
      (ui-person-list list))
    (div :.column
      (ui-person-detail selected-person))))

(def ui-person-picker (comp/factory PersonPicker {:keyfn :person-picker/people}))

(defsc Root [this {:root/keys [person-picker]}]
  {:query         [{:root/person-picker (comp/get-query PersonPicker)}]
   :initial-state {:root/person-picker {}}}
  (js/console.log "Render Root")
  (div :.ui.container.segment
    (h3 "Application")
    (ui-person-picker person-picker)))

(defonce APP (app/fulcro-app {:remotes          {:remote (http/fulcro-http-remote {:url "/api"})}
                              :client-did-mount (fn [app])}))

(defn ^:export init []
  (app/mount! APP Root "app")
  (js/console.log "Loaded"))

(defn ^:export refresh []
  (app/mount! APP Root "app")
  (js/console.log "Hot reload"))
