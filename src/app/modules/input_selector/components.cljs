(ns app.modules.input-selector.components
  (:require [reagent.core :as r]
            [reagent.impl.template :as rtpl]
            [clojure.string :as string]
            [app.modules.input-selector.logic :as logic]
            [app.common.item :as c.item]
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

(defn- render-search-input [^js props]
  (r/create-element TextField
                    (.assign js/Object
                             #js{}
                             props
                             #js{:variant "outlined"
                                 :label "Recipe"})))

(defn- search-field [{:keys [items
                             on-highlight-change]}]
  [:> Autocomplete {:options items
                     ;; Note that the function parameter is a JS Object!
                     ;; Autocomplete expects the renderInput value to be function
                     ;; returning React elements, not a component!
                     ;; So reactify-component won't work here.
                    :full-width true
                    :select-on-focus true
                    :clear-on-blur true
                    :auto-complete true
                    :on-highlight-change on-highlight-change
                    :get-option-label #(-> %
                                           .-name)
                    :render-input render-search-input}])

(defn- filter-item-by-name [target-val [_ v]]
  (string/includes? (string/lower-case v) (string/lower-case target-val)))

(defn- item-selector [_]
  (let [state (r/atom {:filter-target ""})
        handle-filter-change (fn [^js event]
                               (let [value (-> event
                                               .-target
                                               .-value)]
                                 (swap! state assoc :filter-target value)))]
    (fn [{:keys [items on-item-click]}]
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
                        :on-change handle-filter-change}]]]
       [:> Grid {:item true
                 :xs 12}
        [:> Box {:overflow "auto"
                 :max-height "300px"}
         [:> List
          (for [[item-id item-name] (filterv (partial filter-item-by-name (:filter-target @state)) items)]
            ^{:key (str "item-selector-" (name item-id))}
            [:> ListItem {:button true
                          :on-click #(on-item-click item-id)}
             [:> ListItemIcon [:> AddCircle {:color "primary"}]]
             [:> ListItemText item-name]])]]]])))

(defn- selected-inputs [{:keys [items on-item-click]}]
  [:> Grid {:container true
            :spacing 2}
   [:> Grid {:item true}
    [:> Typography {:variant "h5"
                    :component "h2"}
     "Selected Inputs"]]
   [:> Grid {:item true}
    [:> Box {:overflow "auto"
             :max-height "372px"}
     [:> List
      (for [[item-id item-name] items]
        ^{:key (str "selected-inputs-" (name item-id))}
        [:> ListItem {:button true
                      :on-click #(on-item-click item-id)}
         [:> ListItemIcon [:> RemoveCircle {:color "secondary"}]]
         [:> ListItemText item-name]])]]]])

(def default-main-bus-items [:iron-plate :copper-plate :stone :steel-plate :electronic-circuit])

(defn input-selector [{:keys [recipes default-inputs]
                       :or {default-inputs default-main-bus-items}}]
  (let [items (logic/recipes->items recipes)
        state (r/atom {:selected-items (select-keys items default-inputs)
                       :unselected-items (apply dissoc (concat [items] default-inputs))})
        handle-item-selection (fn [item-id]
                                (swap! state update :selected-items assoc item-id (get items item-id))
                                (swap! state update :unselected-items dissoc item-id))
        handle-item-deselection (fn [item-id]
                                  (swap! state update :selected-items dissoc item-id)
                                  (swap! state update :unselected-items assoc item-id (get items item-id)))]
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
