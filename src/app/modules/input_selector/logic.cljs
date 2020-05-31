(ns app.modules.input-selector.logic
  (:require [app.common.item :as c.item]))

(defn recipes->items [recipes]
  (reduce (fn [acc [k]]
            (assoc acc k (c.item/id->name k)))
          {}
          recipes))


