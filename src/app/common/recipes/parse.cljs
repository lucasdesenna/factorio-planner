(ns app.common.recipes.parse
  (:require [app.common.item :as c.item]
            [shadow.resource :as rc]
            [cognitect.transit :as t]))

(defn- k-v-coll? [x]
  (and (coll? x)
       (every? #(and (coll? %)
                     (= 2 (count %)))
               x)))

(defn- keywordize-keys [[k v]]
  [(c.item/name->id k)
   (cond
     (or (map? v)
         (k-v-coll? v))
     (->> v
          (map keywordize-keys)
          (into {}))

     (coll? v)
     (mapv c.item/name->id v)

     :else
     v)])

(defn parse []
  (->> "./0.18.17.json"
       rc/inline
       (t/read (t/reader :json))
       (map keywordize-keys)
       (into {})))