(ns app.modules.factory-previewer.components
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
            [app.modules.factory-previewer.logic :as logic]
            [clojure.string :as string]
            [reagent.core :as r]))

(defn factory-previewer [{:keys [factory-input
                                 factory-input-amount
                                 factory-output]}]
  (let [requirements (-> factory-output
                         (logic/denormalize {:ignored-ids (set factory-input)})
                         (logic/denormalized-item->requirements factory-input-amount))]
    [:div (for [[k {:keys [assemblers consumption-per-sec]}] requirements]
            ^{:key (str "factory-preview-" (name k))}
            [:div
             [:p (name k)]
             [:p (str "Assemblers " assemblers)]
             (for [[k v] consumption-per-sec]
               ^{:key (str "factory-preview-consumption" (name k))}
               [:p (str (name k) " " v)])])]))