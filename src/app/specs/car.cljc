(ns app.specs.car
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::id int?)
(s/def ::make string?)
(s/def ::model string?)

(s/def ::car
  (s/keys
   :req [::id ::make ::model]))
