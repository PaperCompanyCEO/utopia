(ns papercompany.utopia.specs.web.examples.requests.get-test)

(def header-spec
  [:map
   [:msg1 :int]])

(def path-spec
  [:map
   [:msg2 :int]])

(def query-spec
  [:map
   [:msg3 :string]])

(def body-spec
  [:map
   [:msg4 :string]])
