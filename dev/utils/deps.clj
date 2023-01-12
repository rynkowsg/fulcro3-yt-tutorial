(ns utils.deps
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.tools.deps.alpha.repl :refer [add-libs] :as deps-repl]))

(defn refresh-deps
  []
  (let [deps-edn    (-> "deps.edn"
                        (io/file)
                        (slurp)
                        (edn/read-string))
        common-deps (:deps deps-edn)
        dev-deps    (-> deps-edn :aliases :dev :extra-deps)
        deps        (merge common-deps dev-deps)]
    (add-libs deps)
    deps))
