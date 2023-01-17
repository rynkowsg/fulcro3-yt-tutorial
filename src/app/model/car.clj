(ns app.model.car
  (:require
    [app.specs.car :as car]
    [com.wsscode.pathom.connect :as pc]))

(def cars
  (atom {1 {::car/id    1
            ::car/make  "Honda"
            ::car/model "Accord"}
         2 {::car/id    2
            ::car/make  "Ford"
            ::car/model "F-150"}}))

(pc/defresolver car-resolver [env {::car/keys [id] :as params}]
  {::pc/input #{::car/id}
   ::pc/output [::car/id ::car/make ::car/model]}
  (println "car-resolver, params: " params)
  (get @cars id))

(def resolvers [car-resolver])

(comment
 (app.server/parser {} [{[:app.specs.car/id 2] [:app.specs.car/id :app.specs.car/make]}]))
