(ns app.views.main.components
  (:require ["@material-ui/core/Grid" :default Grid]
            ["@material-ui/core/Box" :default Box]
            [app.modules.input-selector.components :refer [input-selector]]
            [app.modules.output-selector.components :refer [output-selector]]
            [app.modules.requirements-viewer.components :refer [requirements-viewer]]
            [app.items :as items]
            [app.recipes :as recipes]
            [reagent.core :as r]))

(def default-input
  [:item.iron-plate
   :item.copper-plate
   :item.stone
   :item.steel-plate
   :item.electronic-circuit])

(defn main-view []
  (let [state (r/atom {:input default-input
                       :output-id nil
                       :output-amount 1})
        on-input-change #(swap! state assoc :input %)
        on-ouput-id-change #(swap! state assoc :output-id %)
        on-ouput-amount-change #(swap! state assoc :output-amount %)]
    (fn []
      (let [{:keys [input output-id output-amount]} @state]
        [:> Box {:p 3}
         [:> Grid {:container true :spacing 2}
          [:> Grid {:item true
                    :xs 12}
           [input-selector {:input input
                            :items items/all
                            :on-input-change on-input-change}]]
          [:> Grid {:item true
                    :xs 12}
           [output-selector {:items items/all
                             :output-id output-id
                             :output-amount output-amount
                             :on-output-id-change on-ouput-id-change
                             :on-output-amount-change on-ouput-amount-change}]]
          [:> Grid {:item true
                    :xs 12}
           [requirements-viewer {:input input
                                 :output-id output-id
                                 :output-amount output-amount
                                 :items items/all
                                 :recipes recipes/all}]]]]))))