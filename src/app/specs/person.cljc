(ns app.specs.person
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::id int?)
(s/def ::name string?)
(s/def ::age pos-int?)
(s/def ::cars any?)

(s/def ::person
  (s/keys
   :req [::id ::name ::age]
   :opt [::cars]))
