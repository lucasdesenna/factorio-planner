(ns app.modules.output-selector.components
  (:require ["@material-ui/core/Box" :default Box]
            ["@material-ui/core/Card" :default Card]
            ["@material-ui/core/CardContent" :default CardContent]
            ["@material-ui/core/CardHeader" :default CardHeader]
            ["@material-ui/core/Grid" :default Grid]
            ["@material-ui/core/Paper" :default Paper]
            ["@material-ui/core/Table" :default Table]
            ["@material-ui/core/TableBody" :default TableBody]
            ["@material-ui/core/TableCell" :default TableCell]
            ["@material-ui/core/TableHead" :default TableHead]
            ["@material-ui/core/TableRow" :default TableRow]
            ["@material-ui/core/TextField" :default TextField]
            ["@material-ui/core/Typography" :default Typography]
            ["@material-ui/lab/Autocomplete" :default Autocomplete]
            [app.common.denormalize.logic :as c.d.logic]
            [clojure.string :as string]
            [reagent.impl.template :as rtpl]
            [reagent.core :as r]))

(defn id->name [id]
  (-> id
      name
      (string/split #"\.")
      last
      (string/replace #"-" " ")
      string/capitalize))

(defn- columns->rows [columns]
  (let [row-count (->> columns
                       (map count)
                       (apply max))]
    (reduce (fn [acc r]
              (conj acc (mapv #(get (vec %) r "") columns)))
            []
            (range row-count))))

(defn- item-details [{:keys [id]
                      item-name :name
                      :as props}]
  (let [attributes (select-keys props [:produced-by :consumed-by])
        header (->> attributes keys (mapv #(string/capitalize (name %))))
        rows (->> attributes vals columns->rows)]
    [:> Grid {:container true}
     [:> Grid {:item true
               :xs 12}
      [:> Typography {:gutter-bottom true
                      :variant "h5"
                      :component "h2"}
       item-name]
      [:> Typography {:gutter-bottom true
                      :color "textSecondary"}
       (str ":" id)]]
     [:> Grid {:item true
               :xs 12}
      [:> Table {:size "small"}
       [:> TableHead
        [:> TableRow
         (for [cell header]
           ^{:key (str item-name "-" cell)}
           [:> TableCell cell])]]
       [:> TableBody
        (for [r (range (count rows))
              :let [row (get rows r)
                    row-key (str item-name "-row-" r)]]
          ^{:key row-key}
          [:> TableRow
           (for [c (range (count row))
                 :let [cell (get row c)]]
             ^{:key (str row-key "-cell-" c)}
             [:> TableCell (if (coll? cell)
                             (str (-> cell first id->name) " x " (second cell))
                             (id->name cell))])])]]]]))

(defn- render-search-input [^js props]
  (r/create-element TextField
                    (.assign js/Object
                             #js{}
                             props
                             #js{:variant "outlined"
                                 :label "Outputs"})))

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

(defn output-selector [{:keys [items
                               output-id
                               output-amount
                               on-output-id-change
                               on-output-amount-change]}]
  (let [state (r/atom {:selected-item (when output-id (c.d.logic/item-id->item output-id))})
        items (vals items)
        handle-output-id-change #(when on-output-id-change (on-output-id-change %))
        handle-output-amount-change #(when on-output-amount-change (on-output-amount-change %))
        handle-item-selection (fn [^js _ item]
                                (let [{:keys [id]
                                       :as item} (js->clj item :keywordize-keys true)]
                                  (swap! state assoc :selected-item item)
                                  (handle-output-id-change (keyword id))))]
    (fn []
      [:> Paper
       [:> Box {:p 3}
        [:> Grid {:container true :spacing 2}
         [:> Grid {:item true
                   :xs 12}
          [search-field {:items items
                         :on-highlight-change handle-item-selection}]]
         (when-let [item (:selected-item @state)]
           [:> Grid {:item true
                     :xs 12}
            [item-details item]])]]])))
