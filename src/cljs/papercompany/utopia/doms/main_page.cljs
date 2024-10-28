(ns papercompany.utopia.doms.main-page
  (:require
   [reagent.core :as reagent]
   [re-frame.alpha :as re-frame-alpha]))

(defn main-page [profile]
  (let [main-page @(re-frame-alpha/subscribe [:page :main])]
    [:div
     [:h2 (str "Hello, World! " profile)]
     [main-page]]))
