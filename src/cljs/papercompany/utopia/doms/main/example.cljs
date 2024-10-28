(ns papercompany.utopia.doms.main.example)

(defn example-page [{:keys [env]}
                    {{:keys [query1]} :query}]
  (fn []
    [:div (str (+ query1 query1) " 캐치 티니핑2222 " env)]))
