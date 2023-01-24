(ns app.model.person
  (:require
   [com.wsscode.pathom.connect :as pc]))

(def people
  (atom {1 {:person/id   1
            :person/name "Bob"
            :person/age  22
            :person/cars #{2}}
         2 {:person/id   2
            :person/name "Sally"
            :person/age  26
            :person/cars #{1}}}))

(comment
 (swap! people assoc-in [1 :person/age] 99)
 (swap! people assoc-in [1 :person/name] "Tony")
 (swap! people update 1 dissoc :person/age))

(pc/defresolver person-resolver [env {:person/keys [id] :as params}]
  {::pc/input  #{:person/id}
   ::pc/output [:person/name :person/age {:person/cars [:car/id]}]}
  (let [person (-> @people
                   (get id)
                   (update :person/cars (fn [ids] (mapv
                                                   (fn [id] {:car/id id})
                                                   ids))))]
    person))
#_(person-resolver {} {:person/id 1})

(pc/defresolver all-people-resolver [env params]
  {::pc/output [:all-people]}
  {:all-people (mapv (fn [i] {:person/id i}) (keys @people))})
#_(all-people-resolver {} {})

(pc/defmutation make-older [env {:person/keys [id]}]
  {::pc/params [:person/id]
   ::pc/output []}
  (swap! people update-in [id :person/age] inc)
  {})                                                       ;; the empty map is returned here intentionally

(def resolvers [person-resolver all-people-resolver make-older])

(comment
 (app.server/parser {} [{[:person/id 2] [:person/name]}])
 (app.server/parser {} [{[:person/id 1] [:person/name :person/cars]}])
 (app.server/parser {} [{[:person/id 1] [:person/cars]}])
 (app.server/parser {} [{[:person/id 1] [{:person/cars [:car/make :car/model]}]}])
 (app.server/parser {} '[{:all-people [*]}])
 (app.server/parser {} '[:all-people])                      ;; same as above
 (app.server/parser {} [{:all-people [:person/id]}])
 (app.server/parser {} [:server/time :all-people])
 (app.server/parser {} [{:all-people [:person/id
                                      :person/name
                                      :person/age
                                      {:person/cars [:car/make]}
                                      :server/time]}])
 (app.server/parser {} [:server/time
                        {:all-people
                         [:person/id
                          :person/name
                          :person/age
                          {:person/cars [:car/make]}]}]))
