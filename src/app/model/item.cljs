(ns app.model.item
  (:require
   [com.fulcrologic.fulcro.mutations :refer [defmutation]]))
;
;(pc/defresolver item-resolver [env {:item/keys [id]}]
;                {::pc/input  #{:item/id}
;                 ::pc/output [:item/title :item/in-stock :item/price]}
;                (get @items id))
;
;(pc/defresolver all-items-resolver [_ _]
;                {::pc/output [:item/all-items]}
;                {:item/all-items (->> items deref vals (sort-by :item/id) vec)})
;
;(defmutation set-item-price [env {:item/keys [id price]}]
;                {::pc/params [:item/id :item/price]
;                 ::pc/output [:item/id]
;                 (when-not (decimal? price)
;                   (throw (ex-info "API INVARIANT VIOLATED!" {:item/price "must be decimal"})))
;                 (swap! items assoc-in [id :item/price] price)
;                 {:item/id id}})
