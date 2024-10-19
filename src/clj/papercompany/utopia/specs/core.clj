(ns papercompany.utopia.specs.core
  (:require
   [malli.generator :as malli-generator]
   [malli.error :as malli-error]
   [malli.core :as malli]
   [cheshire.core :as json]
   [clojure.test.check.generators :as gen]))

(def const-schema
  (malli/-simple-schema
   {:type :const
    :min 1
    :max 1
    :compile (fn [_properties [x] _options]
               {:pred #(= % x)
                :type-properties {:error/fn (fn [error _] (str "should be equal as " x))
                                  :gen/gen (gen/return x)}})}))

(def json-schema
  (malli/-simple-schema
   {:type :json
    :min 1
    :max 1
    :compile (fn [_properties [json-schema] {:keys [registry]}]
               {:pred #(try (let [json (json/parse-string % true)]
                              (malli/validate json-schema json {:registry registry}))
                            (catch Exception _
                              false))
                :type-properties {:error/fn (fn [error _]
                                              (let [value (get-in error [:value])]
                                                (try (let [json (json/parse-string value true)]
                                                       (malli-error/humanize
                                                        (malli/explain json-schema json {:registry registry})))
                                                     (catch Exception _
                                                       "Invalid JSON"))))
                                  :gen/gen (json/generate-string
                                            (malli-generator/generate
                                             json-schema
                                             {:registry registry}))}})}))

(defn registry
  ([]
   (registry nil))
  ([regs]
   (merge
    (malli/predicate-schemas)
    (malli/class-schemas)
    (malli/comparator-schemas)
    (malli/type-schemas)
    (malli/sequence-schemas)
    (malli/base-schemas)
    {:const const-schema
     :json json-schema}
    regs)))
