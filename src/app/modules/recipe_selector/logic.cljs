(ns app.modules.recipe-selector.logic
  (:require [app.common.item :as c.item]))

(defn- adapt-recipe [[k {:keys [recipe producers recipe-output]}]]
  {:id k
   :name (c.item/id->name k)
   :input recipe
   :output (or recipe-output (hash-map k 1))
   :producers producers})

(defn adapt-recipes [recipes]
  (->> recipes
       (mapv adapt-recipe)))

