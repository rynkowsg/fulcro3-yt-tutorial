(ns app.client
  (:require
   [com.fulcrologic.fulcro.algorithms.merge :as merge]
   [com.fulcrologic.fulcro.application :as app]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.dom :as dom]
   [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]))

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
                                 {:id 42 :model "Sienna"}]}}
  (dom/div
    (dom/div "Names: " name)
    (dom/div "Age: " age)
    (dom/button {:onClick #(comp/transact! this [(make-older {:person/id id})])} "Make older")
    (dom/h3 "Cars:")
    (dom/ul
      (map ui-car cars))))

(def ui-person (comp/factory Person {:keyfn :person/id}))

(defsc Sample [this {:root/keys [person] :as props}]
  {:query         [{:root/person (comp/get-query Person)}]
   :initial-state {:root/person {:id 1 :name "Bob"}}}
  (ui-person person))

(defonce APP (app/fulcro-app {}))

(defn ^:export init []
  (app/mount! APP Sample "app")
  (js/console.log "Loaded"))

(defn ^:export refresh []
  (app/mount! APP Sample "app")
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
 (do (reset! (::app/state-atom APP) {:person/id   {1 {:person/id 1 :person/name "Joe" :person/cars [[:car/id 1]]}}
                                     :car/id      {1 {:car/id 1 :car/model "F-124"}}
                                     :root/person [:person/id 1]})
     (app/schedule-render! APP))

 ;; usage of merge-component!
 (reset! (::app/state-atom APP) {})
 (merge/merge-component! APP Person {:person/id 1 :person/name "Joe"})
 (merge/merge-component! APP Person {:person/id 2 :person/name "Sally"})
 (merge/merge-component! APP Person {:person/id 7 :person/name "Sevent"})
 (merge/merge-component! APP Person {:person/id 4 :person/name "Billy" :person/cars [{:car/id 22 :car/model "Escort"}]})
 (merge/merge-component! APP Person {:person/id 5 :person/name "Kasia"}
                         :replace [:root/person])
 (merge/merge-component! APP Person {:person/id 5 :person/name "Kasia" :person/cars [[:car/id 22]]})
 (merge/merge-component! APP Car {:car/id 42 :car/model "F-125"})
 (merge/merge-component! APP Person {:person/id 5 :person/cars [[:car/id 22] [:car/id 42]]})
 (merge/merge-component! APP Car {:car/id 45 :car/model "F-124"}
                         :append [:person/id 5 :person/cars])
 (merge/merge-component! APP Person {:person/id 5 :person/name "Kasia" :person/age 24})
 (swap! (::app/state-atom APP) update-in [:person/id 5 :person/age] inc)

 ;; get ident
 (comp/get-ident Car {:car/id 1})

 ;; get initial state
 (comp/get-initial-state Sample)

 ;; mutations
 (make-older {:person/id 1})
 `(make-older {:person/id 1})
 (comp/transact! APP [(make-older {:person/id 5})])
 (comp/transact! APP [`(make-older {:person/id 5})]))
