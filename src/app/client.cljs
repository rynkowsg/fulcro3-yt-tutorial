(ns app.client
  (:require
   [com.fulcrologic.fulcro.application :as app]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.dom :as dom]
   [com.fulcrologic.fulcro.react.version18 :refer [with-react18]]))

(defsc Sample [this props]
  {}
  (dom/div "Hello World"))

(defonce APP (with-react18 (app/fulcro-app {})))

(defn ^:export init []
  (app/mount! APP Sample "app"))
