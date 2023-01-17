(ns app.server
  (:require
   [app.model.car :as car]
   [app.model.person :as person]
   [clojure.core.async :as a]
   [clojure.core.async :as async]
   [com.fulcrologic.fulcro.server.api-middleware :as fmw :refer [not-found-handler wrap-api]]
   [com.wsscode.pathom.connect :as pc]
   [com.wsscode.pathom.core :as p]
   [ring.middleware.content-type :refer [wrap-content-type]]
   [ring.middleware.not-modified :refer [wrap-not-modified]]
   [ring.middleware.resource :refer [wrap-resource]]
   [taoensso.timbre :as log]))

(pc/defresolver current-system-time [env {:as params}]
  {::pc/output [:server/time]}
  (println "current-system-time resolver, params: " params)
  {:server/time (java.util.Date.)})
#_(current-system-time {} {})


(def my-resolvers [car/resolvers person/resolvers current-system-time])

;; setup for a given connect system
(def parser
  (p/parser
   {::p/env {::p/reader [p/map-reader
                         pc/reader2
                         pc/open-ident-reader]
             ::pc/mutation-join-globals [:tempids]}
    ::p/mutate pc/mutate
    ::p/plugins [(pc/connect-plugin {::pc/register my-resolvers})
                 (p/post-process-parser-plugin p/elide-not-found)
                 (p/env-plugin {::p/process-error (fn [_ err]
                                                    (.printStackTrace err)
                                                    (p/error-str err))})
                 p/error-handler-plugin]}))

(def middleware (-> not-found-handler
                    (wrap-api {:uri    "/api"
                               :parser (fn [query]
                                         (log/info "Process" query)
                                         (parser {} query))})
                    (fmw/wrap-transit-params)
                    (fmw/wrap-transit-response)
                    (wrap-resource "public")
                    wrap-content-type
                    wrap-not-modified))

(comment
 ;; query Pathom index:
 (parser {} [{:all-people [:app.specs.person/name
                           :app.specs.person/age
                           {:all-people [:app.specs.person/id]}]}])

 (parser {} [:server/time]))
