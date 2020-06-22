(ns app.modules.input-selector.components
  (:require [reagent.core :as r]
            [reagent.impl.template :as rtpl]
            [clojure.string :as string]
            ["@material-ui/core/Box" :default Box]
            ["@material-ui/core/List" :default List]
            ["@material-ui/core/ListItem" :default ListItem]
            ["@material-ui/core/ListItemText" :default ListItemText]
            ["@material-ui/core/ListItemIcon" :default ListItemIcon]
            ["@material-ui/icons" :refer (AddCircle RemoveCircle)]
            ["@material-ui/core/Paper" :default Paper]
            ["@material-ui/core/Card" :default Card]
            ["@material-ui/core/Grid" :default Grid]
            ["@material-ui/core/CardHeader" :default CardHeader]
            ["@material-ui/core/CardContent" :default CardContent]
            ["@material-ui/core/Typography" :default Typography]
            ["@material-ui/core/TextField" :default TextField]
            ["@material-ui/lab/Autocomplete" :default Autocomplete]))

(defn- filter-item-by-name [target-val [_ {n :item/name}]]
  (string/includes? (string/lower-case n) (string/lower-case target-val)))

(defn- item-selector [_]
  (let [state (r/atom {:filter-target ""})
        handle-filter-change (fn [^js event]
                               (let [value (-> event
                                               .-target
                                               .-value)]
                                 (swap! state assoc :filter-target value)))
        handle-clear-filter #(swap! state assoc :filter-target "")]
    (fn [{:keys [items on-item-click]}]
      (let [{:keys [filter-target]} @state
            handle-item-click (comp handle-clear-filter on-item-click)]
        [:> Grid {:container true
                  :spacing 2}
         [:> Grid {:item true
                   :xs 12}
          [:> Typography {:variant "h5"
                          :component "h2"}
           "Input list"]]
         [:> Grid {:item true
                   :xs 12}
          [:form
           [:> TextField {:style {:width "100%"}
                          :variant "outlined"
                          :label "Filter"
                          :value filter-target
                          :on-change handle-filter-change}]]]
         [:> Grid {:item true
                   :xs 12}
          [:> Box {:overflow "auto"
                   :max-height "300px"}
           [:> List
            (for [[item-id {item-name :item/name}] (filterv (partial filter-item-by-name (:filter-target @state)) items)]
              ^{:key (str "item-selector-" (name item-id))}
              [:> ListItem {:button true
                            :on-click #(handle-item-click item-id)}
               [:> ListItemIcon [:> AddCircle {:color "primary"}]]
               [:> ListItemText item-name]])]]]]))))

(defn- selected-inputs [{:keys [items on-item-click]}]
  [:> Grid {:container true
            :spacing 2}
   [:> Grid {:item true
             :xs 12}
    [:> Typography {:variant "h5"
                    :component "h2"}
     "Selected Inputs"]]
   [:> Grid {:item true
             :xs 12}
    [:> Box {:overflow "auto"
             :max-height "372px"}
     [:> List
      (for [[item-id {item-name :item/name}] items]
        ^{:key (str "selected-inputs-" (name item-id))}
        [:> ListItem {:button true
                      :on-click #(on-item-click item-id)}
         [:> ListItemIcon [:> RemoveCircle {:color "secondary"}]]
         [:> ListItemText item-name]])]]]])

(defn input-selector [{:keys [items
                              input
                              on-input-change]
                       :or {input []}}]
  (let [state (r/atom {:selected-items (select-keys items input)
                       :unselected-items (apply dissoc (concat [items] input))})
        handle-input-change #(when on-input-change
                               (on-input-change (:selected-items @state)))
        handle-item-selection (fn [item-id]
                                (swap! state update :selected-items assoc item-id (get items item-id))
                                (swap! state update :unselected-items dissoc item-id)
                                (handle-input-change))
        handle-item-deselection (fn [item-id]
                                  (swap! state update :selected-items dissoc item-id)
                                  (swap! state update :unselected-items assoc item-id (get items item-id))
                                  (handle-input-change))]
    (fn []
      [:> Paper
       [:> Box {:p 3}
        [:> Grid {:container true :spacing 2}
         [:> Grid {:item true
                   :xs 6}
          [item-selector {:items (:unselected-items @state)
                          :on-item-click handle-item-selection}]]
         [:> Grid {:item true
                   :xs 6}
          [selected-inputs {:items (:selected-items @state)
                            :on-item-click handle-item-deselection}]]]]])))
