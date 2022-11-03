(ns app.client
  (:require
    ["react-number-format" :refer (NumericFormat)]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.algorithms.denormalize :as fdn]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.algorithms.react-interop :as interop]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom :refer [button div h3 label ul]]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.rendering.ident-optimized-render :as ident-optimized]))

(def ui-number-format (interop/react-factory NumericFormat))

(defsc Car [this {:car/keys [id model]}]
  {:query         [:car/id :car/model]
   :ident         :car/id
   :initial-state {:car/id    :param/id
                   :car/model :param/model}}
  (js/console.log "Render car " id)
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
   ;;:shouldComponentUpdate (fn [this props state] true) ;; true is default React behaviour
   :componentDidMount (fn [this] (let [p (comp/props this)]
                                   (js/console.log "Mounted" p)))
   :initLocalState (fn [this props]
                     {:a 2
                      :onClick (fn [evt] (js/console.log "OnClick action"))})}
  (js/console.log "Render person " id)
  (let [state (comp/get-state this)
        onClick (comp/get-state this :onClick)]
    #_(js/console.log "State" state)
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
             (button :.ui.button {:onClick #(comp/transact!
                                              this
                                              `[(make-older ~{:person/id id})]
                                              {:refresh [:person-list/people]})}
                     "Make Older")
             (h3 {} "Cars:")
             (ul {}
                 (map ui-car cars))))))

(def ui-person (comp/factory Person {:keyfn :person/id}))

(defsc PersonList [this {:person-list/keys [people] :as props}]
  {:query [{:person-list/people (comp/get-query Person)}]
   :ident (fn [_ _] [:component/id ::person-list])
   :initial-state {:person-list/people [{:id 1 :name "Bob"}
                                        {:id 2 :name "Sally"}]}}
  (js/console.log "Render list")
  (let [cnt (->> people (reduce (fn [c {:person/keys [age]}] (if (> age 30) (inc c) c)) 0))]
    (div :.ui.segment
      (h3 "People")
      (div "Over 30: " cnt)
      (ul
        (map ui-person people)))))

(def ui-person-list (comp/factory PersonList))

(defsc Root [this {:root/keys [list] :as props}]
  {:query         [{:root/list (comp/get-query PersonList)}]
   :initial-state {:root/list {}}}
  (js/console.log "Render root")
  (div
    (h3 "Application")
    (when list
      (ui-person-list list))))


;(defonce APP (app/fulcro-app))
(defonce APP (app/fulcro-app {:optimized-render! ident-optimized/render!})) ;; renders all parent components
;(defonce APP (app/fulcro-app {:optimized-render! keyframe/render!})) ;; renders all parent components

(defn ^:export init []
  (app/mount! APP Root "app"))

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
  (comp/get-query Root)
  (comp/get-initial-state Person {:id 2 :name "Bob"})
  (comp/get-initial-state Root))

(comment
  (merge/merge-component! APP Person {:person/id 1 :person/name "Joe" :person/age 20} :replace [:root/person])

  (make-older {:person/id 1})
  `(make-older {:person/id 1})

  (app/current-state APP)
  (comp/transact! APP [(make-older {:person/id 1})]))

(comment ;; video 4 - Components DOM and React
  (comp/component-options Person)

  ;; video 5 - indexes when components mount
  (comp/class->all APP Person)
  (comp/class->any APP Person)
  (comp/prop->classes APP :person/age)
  (comp/prop->classes APP :car/model))



(comment
  ;; video 5 - show me ids of all components rendered
  (defn get-component-that-query-for-a-prop
    [prop]
    (reduce
      (fn [mounted-instances cls]
        (concat mounted-instances
                (comp/class->all APP (comp/registry-key->class cls))))
      []
      (comp/prop->classes APP prop)))
  (->> (get-component-that-query-for-a-prop :person/age)
       (map comp/get-ident))

  ;; video 5 -
  (def before (app/current-state APP))
  (comp/transact! APP [(make-older {:person/id 2})])
  (def after (app/current-state APP))
  before
  after

  ;; get props for the subtree conta ining updated Person [:person/id 1]
  (let [state (app/current-state APP)
        component-query (comp/get-query Person)
        component-ident [:person/id 1]
        starting-entity (get-in state component-ident)]
    (fdn/db->tree component-query starting-entity state)))

