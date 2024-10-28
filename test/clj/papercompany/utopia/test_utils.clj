(ns papercompany.utopia.test-utils
  (:require
   [malli.core :as malli]
   [malli.generator :as malli-generator]
   [clojure.pprint :as pprint]))

(defn domain-test [{:keys [name
                           testers
                           writer]}]
  (.write writer
          (str
           "* "
           name
           "\n"))
  (doseq [tester testers]
    (tester))
  (.write writer "\n"))

(defn tester [{:keys [f
                      name
                      dom-spec
                      codom-spec
                      fx-cofx-spec
                      registry
                      fx-cofx
                      transactions
                      writer]}]
  (fn []
    (.write writer
            (str
             "** "
             name
             "\n"))
    (when (reduce
           (fn [acc _]
             (let [input (malli-generator/generate dom-spec {:registry registry})]
               (reset! fx-cofx [])
               (reset! transactions {:next-id 0
                                     :map {}})
               (try (let [output (f input)
                          output-error (malli/explain
                                        (codom-spec input @fx-cofx)
                                        output
                                        {:registry registry})
                          fx-cofx-error (malli/explain
                                         (fx-cofx-spec input @fx-cofx)
                                         @fx-cofx
                                         {:registry registry})]
                      (if (or output-error fx-cofx-error)
                        (do
                          (.write
                           writer
                           (str
                            "*** input\n"
                            "#+BEGIN_SRC clojure :results none :exports code\n"))
                          (pprint/pprint input writer)
                          (.write
                           writer
                           "#+END_SRC\n")
                          (.write
                           writer
                           (str
                            "*** fx-cofx\n"
                            "#+BEGIN_SRC clojure :results none :exports code\n"))
                          (pprint/pprint @fx-cofx writer)
                          (.write
                           writer
                           "#+END_SRC\n")
                          (when output-error
                            (.write
                             writer
                             (str
                              "*** output-error\n"
                              "#+BEGIN_SRC clojure :results none :exports code\n"))
                            (pprint/pprint (dissoc output-error :schema) writer)
                            (.write
                             writer
                             "#+END_SRC\n"))
                          (when fx-cofx-error
                            (.write
                             writer
                             (str
                              "*** fx-cofx-error\n"
                              "#+BEGIN_SRC clojure :results none :exports code\n"))
                            (pprint/pprint (dissoc fx-cofx-error :schema) writer)
                            (.write
                             writer
                             "#+END_SRC\n"))
                          (reduced false))
                        acc))
                    (catch Exception e
                      (.write
                       writer
                       (str
                        "*** input\n"
                        "#+BEGIN_SRC clojure :results none :exports code\n"))
                      (pprint/pprint input writer)
                      (.write
                       writer
                       "#+END_SRC\n")
                      (.write
                       writer
                       (str
                        "*** fx-cofx\n"
                        "#+BEGIN_SRC clojure :results none :exports code\n"))
                      (pprint/pprint @fx-cofx writer)
                      (.write
                       writer
                       "#+END_SRC\n")
                      (.write
                       writer
                       (str
                        "*** EXCEPTION\n"
                        "#+BEGIN_SRC clojure :results none :exports code\n"))
                      (pprint/pprint e writer)
                      (.write
                       writer
                       (str
                        "#+END_SRC\n"))
                      (reduced false)))))
           true
           (range 100))
      (.write writer "OK!\n"))))
