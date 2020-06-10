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

(defn- recipe-details [{:keys [id]
                        recipe-name :name
                        :as props}]
  (let [attributes (select-keys props [:input :producers :output])
        header (->> attributes keys (mapv #(string/capitalize (name %))))
        rows (->> attributes vals columns->rows)]
    [:> Grid {:container true}
     [:> Grid {:item true
               :xs 12}
      [:> Typography {:gutter-bottom true
                      :variant "h5"
                      :component "h2"}
       recipe-name]
      [:> Typography {:gutter-bottom true
                      :color "textSecondary"}
       (str ":" id)]]
     [:> Grid {:item true
               :xs 12}
      [:> Table {:size "small"}
       [:> TableHead
        [:> TableRow
         (for [cell header]
           ^{:key (str recipe-name "-" cell)}
           [:> TableCell cell])]]
       [:> TableBody
        (for [r (range (count rows))
              :let [row (get rows r)
                    row-key (str recipe-name "-row-" r)]]
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
                                 :label "Recipe"})))

(defn- search-field [{:keys [recipes
                             on-highlight-change]}]
  [:> Autocomplete {:options recipes
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

(defn output-selector [{:keys [recipes]}]
  (let [recipes (vals recipes)
        state (r/atom {:selected-recipe nil})
        handle-recipe-selection (fn [^js _ recipe]
                                  (swap! state assoc :selected-recipe (js->clj recipe :keywordize-keys true)))]
    (fn []
      [:> Paper
       [:> Box {:p 3}
        [:> Grid {:container true :spacing 2}
         [:> Grid {:item true
                   :xs 12}
          [search-field {:recipes recipes
                         :on-highlight-change handle-recipe-selection}]]
         (when-let [recipe (:selected-recipe @state)]
           [:> Grid {:item true
                     :xs 12}
            [recipe-details recipe]])]]])))