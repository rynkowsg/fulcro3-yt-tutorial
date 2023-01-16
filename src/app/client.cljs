(ns app.client
  (:require
   [com.fulcrologic.fulcro.algorithms.merge :as merge]
   [com.fulcrologic.fulcro.application :as app]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.dom :as dom]))

(defsc Car [this {:car/keys [id model]}]
  {:query [:car/id :car/model]
   :ident :car/id}
  (dom/div "Model: " model))

(def ui-car (comp/factory Car {:keyfn :car/id}))

(defsc Person [this {:person/keys [id name age cars] :as props}]
  {:query [:person/id :person/name :person/age {:person/cars (comp/get-query Car)}]
   :ident :person/id}
  (dom/div
   (dom/div "Names: " name)
   (dom/div "Age: " age)
   (dom/h3 "Cars:")
   (dom/ul
    (map ui-car cars))))

(def ui-person (comp/factory Person {:keyfn :person/id}))

(defsc Sample [this {:root/keys [person] :as props}]
  {:query [{:root/person (comp/get-query Person)}]}
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
 (comp/get-ident Car {:car/id 1}))
