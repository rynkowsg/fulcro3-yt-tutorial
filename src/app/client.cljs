(ns app.client
  (:require
    ["react-number-format" :refer (NumericFormat)]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.algorithms.react-interop :as interop]
    [com.fulcrologic.fulcro.dom :as dom :refer [button div h3 label ul]]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]))

(def ui-number-format (interop/react-factory NumericFormat))

(defsc Car [this {:car/keys [id model]}]
  {:query         [:car/id :car/model]
   :ident         :car/id
   :initial-state {:car/id    :param/id
                   :car/model :param/model}}
  (dom/div "Model: " model))

(def ui-car (comp/factory Car {:keyfn :car/id}))

(defmutation make-older [{:person/keys [id]}]
  (action [{:keys [state]}]
          (swap! state update-in [:person/id id :person/age] inc)))

(defsc Person [this {:person/keys [id name age cars] :as props}]
  {:query         [:person/id :person/name :person/age {:person/cars (comp/get-query Car)}]
   :ident         :person/id
   :initial-state {:person/id   :param/id
                   :person/name :param/name
                   :person/age  20
                   :person/cars [{:id 40 :model "Leaf"}
                                 {:id 41 :model "Escort"}
                                 {:id 42 :model "Sienna"}]}
   :shouldComponentUpdate (fn [this props state] true)
   :componentDidMount (fn [this] (let [p (comp/props this)]
                                   (js/console.log "Mounted" p)))
   :initLocalState (fn [this props]
                     {:a 2
                      :onClick (fn [evt] (js/console.log "OnClick action"))})}
  (let [state (comp/get-state this)
        onClick (comp/get-state this :onClick)]
    (js/console.log "State" state)
    (div :.ui.segment {}
        (div :.ui.form {}
             (div :.field {}
                  (label {} "Name: ")
                  name)
             (div :.field {}
               (label {} "Amount: ")
               (ui-number-format {:thousandSeparator true :prefix "$"}))
             (div :.field {}
                  (label {:onClick onClick} "Age: ")
                  age)
             (button {:onClick #(comp/transact! this [(make-older {:person/id id})])} "Make older")
             (h3 {} "Cars:")
             (ul {}
                 (map ui-car cars))))))

(def ui-person (comp/factory Person {:keyfn :person/id}))

(defsc PersonList [this {:person-list/keys [people] :as props}]
  {:query [{:person-list/people (comp/get-query Person)}]
   :ident (fn [_ _] [:component/id ::person-list])
   :initial-state {:person-list/people [{:id 1 :name "Bob"}
                                        {:id 2 :name "Sally"}]}}
  (div
    (h3 "People")
    (map ui-person people)))

(def ui-person-list (comp/factory PersonList))

(defsc Sample [this {:root/keys [people] :as props}]
  {:query         [{:root/people (comp/get-query PersonList)}]
   :initial-state {:root/people {}}}
  (div
    (when people
      (ui-person-list people))))

(defonce APP (app/fulcro-app))

(defn ^:export init []
  (app/mount! APP Sample "app"))

(comment
  (keys APP)
  (-> APP (::app/state-atom) deref)

  (app/current-state APP)

  (app/schedule-render! APP)

  (reset! (::app/state-atom APP) {})
  (do
    (reset! (::app/state-atom APP) {:person/id 1 :person/name "Joe" :person/cars [{:car/id 22 :car/model "Escort"}]})
    (app/schedule-render! APP))

  (merge/merge-component! APP Person {:person/id 1 :person/name "Joe"})
  (merge/merge-component! APP Person {:person/id 2 :person/name "Sally"})
  (merge/merge-component! APP Person {:person/id 7 :person/name "Sevent"})
  (merge/merge-component! APP Person {:person/id 4 :person/name "Billy" :person/cars [{:car/id 22 :car/model "Escort"}]})
  (merge/merge-component! APP Person {:person/id 5 :person/name "Kasia"}
                          :replace [:root/person])
  (merge/merge-component! APP Person {:person/id 5 :person/name "Kasia" :person/cars [[:car/id 22]]})
  (merge/merge-component! APP Car {:car/id 42 :car/model "F-125"})
  (merge/merge-component! APP Person {:person/id 5 :person/cars [[:car/id 22] [:car/id 42]]})
  (merge/merge-component! APP Car {:car/id 45 :car/model "F-124"} :append [:person/id 5 :person/cars])
  (do
    (swap! (::app/state-atom APP) assoc-in [:person/id 5 :person/age] 25)
    (app/schedule-render! APP))

  (comp/get-ident Car {:car/id 22})
  (comp/get-query Sample)
  (comp/get-initial-state Person {:id 2 :name "Bob"})
  (comp/get-initial-state Sample))

(comment
  (merge/merge-component! APP Person {:person/id 1 :person/name "Joe" :person/age 20} :replace [:root/person])

  (make-older {:person/id 1})
  `(make-older {:person/id 1})

  (app/current-state APP)
  (comp/transact! APP [(make-older {:person/id 1})]))

(comment ;; video 4 - Components DOM and React
  (comp/component-options Person))
