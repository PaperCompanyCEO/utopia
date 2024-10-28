(ns papercompany.utopia.doms.main.home)

(defn home-page [{:keys [env]}
                 {}]
  (fn []
    [:div (str "홈페이지: 캐치 티니핑 " env)]))
