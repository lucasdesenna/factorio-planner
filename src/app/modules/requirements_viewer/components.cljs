(ns app.modules.requirements-viewer.components
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
            [app.modules.requirements-viewer.logic :as logic]
            [clojure.string :as string]
            [reagent.core :as r]))

(defn requirements-viewer [{:keys [input
                                   output-id
                                   output-amount
                                   items
                                   recipes]}]
  (if-let [requirements (some-> output-id
                                (c.d.logic/denormalize {:ignored-ids (set input)
                                                        :items items
                                                        :recipes recipes})
                                (logic/denormalized-item->requirements output-amount))]
    [:div (for [[k {:keys [assemblers consumption-per-sec]}] requirements]
            ^{:key (str "factory-preview-" (name k))}
            [:div
             [:p (name k)]
             [:p (str "Assemblers " assemblers)]
             (for [[k v] consumption-per-sec]
               ^{:key (str "factory-preview-consumption" (name k))}
               [:p (str (name k) " " v)])])]
    [:p "Select inputs and outputs for your factory."]))