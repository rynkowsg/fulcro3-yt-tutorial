(ns app.client
  (:require
   [app.model.person :refer [make-older picker-path select-person]]
   [com.fulcrologic.fulcro.application :as app]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.data-fetch :as df]
   [com.fulcrologic.fulcro.dom :as dom :refer [div ul li button h3 label a input table tr td th thead tbody]]
   [com.fulcrologic.fulcro.dom.events :as evt]
   [com.fulcrologic.fulcro.mutations :as m]
   [com.fulcrologic.fulcro.networking.http-remote :as http]))

(defsc ItemListItem [this {:item/keys [id title in-stock price] :as props}]
  {:query [:item/id :item/title :item/in-stock :item/price]
   :ident :item/id
   :initial-state {:item/title "Test Title"}}
  (tr
   (td title)
   (td (input {:value    in-stock
               :type     "number"
               :onChange (fn [evt]
                           (js/console.log (type (evt/target-value evt)))
                           (m/set-value! this :item/in-stock (evt/target-value evt)))}))
   #_(td (str price))))

(def ui-item-list-item (comp/factory ItemListItem {:keyfn :item/id}))

(defsc ItemList [this {:item-list/keys [all-items] :as props}]
  {:query [{:item-list/all-items (comp/get-query ItemListItem)}]
   :initial-state {:item-list/all-items []}
   :ident (fn [] [:component/id ::item-list])}
  (table :.ui.table
    (thead (tr (th "Title") (th "# In Stock") (th "Price")))
    (tbody (map ui-item-list-item all-items))))

(def ui-item-list (comp/factory ItemList {:keyfn :item-list/all-items}))

(defsc Root [this {:root/keys [item-list]}]
  {:query         [{:root/item-list (comp/get-query ItemList)}]
   :initial-state {:root/item-list {}}}
  (div :.ui.container.segment
    (h3 "Inventory Items")
    (ui-item-list item-list)))

(defonce APP (app/fulcro-app {:remotes          {:remote (http/fulcro-http-remote {})}
                              :client-did-mount (fn [app]
                                                  (df/load! app :item/all-items
                                                     ItemListItem
                                                     {:target [:component/id ::item-list :item-list/all-items]}))}))

(defn ^:export init []
  (app/mount! APP Root "app")
  (js/console.log "Loaded"))

(defn ^:export refresh []
  (app/mount! APP Root "app")
  (js/console.log "Hot reload"))

(comment
 (df/load! APP [:person/id 1] PersonListItem))
