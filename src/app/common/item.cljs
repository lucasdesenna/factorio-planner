(ns app.common.item
  (:require [clojure.string :as string]))

(defn name->id [name]
  (-> name
      string/lower-case
      (string/replace #" " "-")
      keyword))

(defn id->name [id]
  (-> id
      name
      (string/replace #"-" " ")
      string/capitalize))