(ns raw-data-parser.parse
  (:require [clojure.string :as string]
            [shadow.resource :as rc]
            [cognitect.transit :as t]))

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

(defn- prefixate-keyword [prefix k]
  (->> k
       name
       (str prefix ".")
       keyword))

(defn- k-v-coll? [x]
  (and (coll? x)
       (every? #(and (coll? %)
                     (= 2 (count %)))
               x)))

(defn- keywordize-keys [[k v]]
  [(name->id k)
   (cond
     (or (map? v)
         (k-v-coll? v))
     (->> v
          (map keywordize-keys)
          (into {}))

     (coll? v)
     (mapv name->id v)

     :else
     v)])

(def kw->recipe-kw (partial prefixate-keyword "recipe"))
(def kw->item-kw (partial prefixate-keyword "item"))
(def kw->producer-kw (partial prefixate-keyword "producer"))


(defn- adapt-recipe [[k {:keys [recipe producers recipe-output]}]]
  (let [recipe-kw (kw->recipe-kw k)
        input (->> (dissoc recipe :time)
                   (map (fn [[k v]] [(kw->item-kw k) v]))
                   (into {}))
        output (->> (or recipe-output {recipe-kw 1})
                    (map (fn [[k v]] [(kw->item-kw k) v]))
                    (into {}))]
    [recipe-kw #:recipe {:id recipe-kw
                         :name (id->name k)
                         :time (:time recipe)
                         :input input
                         :output output
                         :producers (mapv kw->producer-kw producers)}]))

(defn parse-recipes []
  (->> "./0.18.17.json"
       rc/inline
       (t/read (t/reader :json))
       (map (comp adapt-recipe keywordize-keys))
       (into {})))

#_(parse-recipes)

(defn- merge-items [item1 item2]
  (let [merge-fn (fn [x y]
                   (if (sequential? y)
                     (vec (concat [] x y))
                     y))]
    (merge-with merge-fn item1 item2)))

(defn ->items [recipe-id relationship-kw recipe-fragment]
  (reduce (fn [acc id]
            (let [item-kw (kw->item-kw  id)]
              (merge-with merge-items
                          acc
                          {item-kw (assoc {:item/id item-kw
                                           :item/name (id->name id)}
                                          relationship-kw [recipe-id])})))
          {}
          recipe-fragment))

(defn- adapt-item [items [recipe-id {:keys [recipe-output]
                                     recipe-input :recipe}]]
  (let [recipe-kw (kw->recipe-kw recipe-id)
        items-consumed-by-recipe (->> (dissoc recipe-input :time)
                                      keys
                                      (->items recipe-kw :item/consumed-by))
        items-produced-by-recipe (->> (or recipe-output {recipe-id 1})
                                      keys
                                      (->items recipe-kw :item/produced-by))]
    (merge-with merge-items
                items
                items-consumed-by-recipe
                items-produced-by-recipe)))

(defn parse-items []
  (->> "./0.18.17.json"
       rc/inline
       (t/read (t/reader :json))
       (map keywordize-keys)
       (into {})
       (reduce adapt-item {})))

#_(parse-items)