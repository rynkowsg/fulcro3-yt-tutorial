(ns app.model.person
  (:require
   [app.specs.car :as car]
   [app.specs.person :as person]
   [com.wsscode.pathom.connect :as pc]))

(def people
  (atom {1 {::person/id   1
            ::person/name "Bob"
            ::person/age  22
            ::person/cars #{2}}
         2 {::person/id   2
            ::person/name "Sally"
            ::person/age  26
            ::person/cars #{1}}}))

(pc/defresolver person-resolver [env {::person/keys [id] :as params}]
  {::pc/input  #{::person/id}
   ::pc/output [::person/name ::person/age {::person/cars [::car/id]}]}
  (println "person-resolver, params: " params)
  (let [person (-> @people
                   (get id)
                   (update ::person/cars (fn [ids] (mapv
                                                    (fn [id] {::car/id id})
                                                    ids))))]
    person))
#_(person-resolver {} {::person/id 1})

(pc/defresolver all-people-resolver [env {:as params}]
  {::pc/output [:all-people]}
  (println "all-people-resolver, params: " params)
  {:all-people (mapv (fn [i] {::person/id i}) (keys @people))})
#_(all-people-resolver {} {})

(pc/defmutation make-older [env {::person/keys [id]}]
  {::pc/params [::person/id]
   ::pc/output []}
  (do
    (println "make-older server")
    (swap! people update-in [id ::person/age] inc)
    {}))                                                    ;; the empty map is returned here intentionally

(def resolvers [person-resolver all-people-resolver make-older])

(comment
 (app.server/parser {} [{[:app.specs.person/id 2] [:app.specs.person/name]}])
 (app.server/parser {} [{[:app.specs.person/id 1] [:app.specs.person/name :app.specs.person/cars]}])
 (app.server/parser {} [{[:app.specs.person/id 1] [:app.specs.person/cars]}])
 (app.server/parser {} [{[:app.specs.person/id 1] [{:app.specs.person/cars [:app.specs.car/make :app.specs.car/model]}]}])
 (app.server/parser {} '[{:all-people [*]}])
 (app.server/parser {} '[:all-people])                      ;; same as above
 (app.server/parser {} [{:all-people [:app.specs.person/id]}])
 (app.server/parser {} [:server/time :all-people])
 (app.server/parser {} [{:all-people [:app.specs.person/id
                                      :app.specs.person/name
                                      :app.specs.person/age
                                      {:app.specs.person/cars [:app.specs.car/make]}
                                      :server/time]}])
 (app.server/parser {} [:server/time
                        {:all-people
                         [:app.specs.person/id
                          :app.specs.person/name
                          :app.specs.person/age
                          {:app.specs.person/cars [:app.specs.car/make]}]}]))
