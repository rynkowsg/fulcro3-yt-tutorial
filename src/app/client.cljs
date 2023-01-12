(ns app.client
  (:require
   ["react-number-format" :refer (NumericFormat)]
   [com.fulcrologic.fulcro.algorithms.merge :as merge]
   [com.fulcrologic.fulcro.algorithms.react-interop :as interop]
   [com.fulcrologic.fulcro.application :as app]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.dom :as dom :refer [button div h3 label ul]]
   [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
   [com.fulcrologic.fulcro.rendering.keyframe-render :as keyframe]))

(def ui-number-format (interop/react-factory NumericFormat))

(defsc Car [this {:car/keys [id model]}]
  {:query         [:car/id :car/model]
   :ident         :car/id
   :initial-state {:car/id    :param/id
                   :car/model :param/model}}
  (js/console.log "Render car" id)
  (div {} "Model: " model))

(def ui-car (comp/factory Car {:keyfn :car/id}))

(defmutation make-older [{:person/keys [id]}]
  (action [{:keys [state]}]
    (swap! state update-in [:person/id id :person/age] inc)))

(defsc Person [this {:person/keys [id name age cars] :as props}]
  {:query             [:person/id :person/name :person/age {:person/cars (comp/get-query Car)}]
   :ident             :person/id
   :initial-state     {:person/id   :param/id
                       :person/name :param/name
                       :person/age  20
                       :person/cars [{:id 40 :model "Leaf"}
                                     {:id 41 :model "Escort"}
                                     {:id 42 :model "Sienna"}]}
   :componentDidMount (fn [this] (let [p (comp/props this)]
                                   (js/console.log "Mounted" p)))
   :initLocalState    (fn [this {:person/keys [id name age cars] :as props}]
                        {:anything :can-be-added-here
                         :onClick  (fn [evt]
                                     (comp/transact! this [(make-older {:person/id id})])
                                     (js/console.log "Made" name "older from cached function"))})}
  (js/console.log "Render person" id)
  (let [onClick (comp/get-state this :onClick)]
    (div :.ui.segment {}
      (div :.ui.form {}
        (div :.field {}
          (label {} "Name: ")
          name)
        (div :.field {}
          (label {} "Amount:")
          (ui-number-format {:value             "100000200.00" ;; values in HTML components are always strings
                             :thousandSeparator true
                             :prefix            "$"}))
        (div :.field {}
          (label {:onClick onClick} "Age: ")
          age))
      (button {:onClick #(do (comp/transact! this [(make-older {:person/id id})])
                             (js/console.log "Made" name "older from inline function"))}
        "Make older")
      (h3 {} "Cars:")
      (ul {}
        (map ui-car cars)))))

(def ui-person (comp/factory Person {:keyfn :person/id}))

;; We have only one PersonList in the app, therefore we don't need an id for the list.
(defsc PersonList [this {:person-list/keys [people] :as props}]
  {:query         [{:person-list/people (comp/get-query Person)}]
   :ident         (fn [_ _] [:component/id ::person-list])
   :initial-state {:person-list/people [{:id 1 :name "Bob"}
                                        {:id 2 :name "Sally"}]}}
  (js/console.log "Render person list")
  (div {}
    (h3 {} "People")
    (map ui-person people)))


(def ui-person-list (comp/factory PersonList))

(defsc Root [this {:root/keys [list] :as props}]
  {:query         [{:root/list (comp/get-query PersonList)}]
   :initial-state {:root/list {}}}
  (js/console.log "Render root")
  (div {}
    (h3 {} "Application")
    (ui-person-list list)))

(defonce APP (app/fulcro-app {:optimized-render! keyframe/render!}))

(defn ^:export init []
  (app/mount! APP Root "app")
  (js/console.log "Loaded"))

(defn ^:export refresh []
  (app/mount! APP Root "app")
  (js/console.log "Hot reload"))


(comment
 (keys APP)

 ;; check state
 (-> APP (::app/state-atom) deref)
 (app/current-state APP)

 ;; clear state & render
 (do (reset! (::app/state-atom APP) {})
     (app/schedule-render! APP))

 ;; set state arbitrarily & request render
 (do (reset! (::app/state-atom APP) {:root/list    [:component/id :app.client/person-list]
                                     :component/id {::person-list {:person-list/people [[:person/id 1]]}}
                                     :person/id    {1 {:person/id 1 :person/name "Joe" :person/cars [[:car/id 1]]}}
                                     :car/id       {1 {:car/id 1 :car/model "F-124"}}})
     (app/schedule-render! APP))

 ;; usage of merge-component!
 ;; - reset the state
 (reset! (::app/state-atom APP) {})
 ;; - change names of default people
 (merge/merge-component! APP Person {:person/id 1 :person/name "Joe"})
 (merge/merge-component! APP Person {:person/id 2 :person/name "Sam"})
 ;; - add Kasia and add her to list
 (merge/merge-component! APP Person {:person/id 5 :person/name "Kasia" :person/cars [{:car/id 22 :car/model "Escort"}]}
                         :prepend [:component/id ::person-list :person-list/people])
 ;; - add to her more cars
 (merge/merge-component! APP Person {:person/id 5 :person/cars [[:car/id 22] {:car/id 42 :car/model "F-125"}]})
 (merge/merge-component! APP Car {:car/id 45 :car/model "F-124"}
                         :append [:person/id 5 :person/cars])
 ;; - make her the only person in the list
 (merge/merge-component! APP PersonList {:person-list/people [[:person/id 5]]})
 (merge/merge-component! APP PersonList {:person-list/people [{:person/id 5 :person/name "Kasia"}]}) ;; same as above
 ;; - replace the list with Greg
 (merge/merge-component! APP PersonList {:person-list/people [{:person/id 6 :person/name "Greg" :person/cars [{:car/id 1033 :car/model "Toyota"}]}]})
 (merge/merge-component! APP PersonList {:person-list/people [{:person/id 6 :person/name "Greg" :person/cars [{:car/id 1033 :car/model "Toyota"}]}]}
                         :replace [:root/list])             ;; in this particular case this :replace do nothing because PersonList is singleton
 ;; - add one car to Greg
 (merge/merge-component! APP Car {:car/id 45 :car/model "F-124"}
                         :append [:person/id 6 :person/cars])

 ;; get ident
 (comp/get-ident Car {:car/id 1})

 ;; get initial state
 (comp/get-initial-state Sample)

 ;; mutations
 (make-older {:person/id 1})
 `(make-older {:person/id 1})
 (comp/transact! APP [(make-older {:person/id 5})])
 (comp/transact! APP [`(make-older {:person/id 5})]))
