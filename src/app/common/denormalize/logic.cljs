(ns app.common.denormalize.logic
  (:require [app.recipes :as recipes]
            [app.items :as items]))

(defn item-id->item
  ([id] (item-id->item id items/all))

  ([id all-items]
   (if-let [item (get all-items id)]
     item
     (throw (js/Error. (str "No Item of ID " id " found."))))))

(defn recipe-id->recipe
  ([id] (recipe-id->recipe id recipes/all))

  ([id all-recipes]
   (if-let [recipe (get all-recipes id)]
     recipe
     (throw (js/Error. (str "No Recipe of ID " id " found."))))))

(defn- denorm-item [{:item/keys [produced-by]
                     :as item}
                    denorm-fn
                    {:keys [ignored-ids]
                     :or {ignored-ids #{}}
                     :as options}]

  ;; TODO: Deal with items with multiple producers
  ;; e.g.: 
  ;; :item.petroleum-gas
  ;; :item.lubricant
  ;; :item.light-oil
  ;; :item.heavy-oil

  (let [filtered-produced-by (some->> produced-by
                                      (remove (partial contains? ignored-ids)))]
    (if (and filtered-produced-by
             (= 1 (count filtered-produced-by)))
      (assoc item
             :item/produced-by (-> filtered-produced-by
                                   first
                                   (denorm-fn options)))
      (dissoc item :item/produced-by))))

(defn- denorm-recipe [{:recipe/keys [input]
                       :as recipe}
                      denorm-fn
                      {:keys [ignored-ids]
                       :or {ignored-ids #{}}
                       :as options}]

  (if (seq input)
    (assoc recipe
           :recipe/input (->> input
                              (remove #(contains? ignored-ids (first %)))
                              (map (fn [[id amount]]
                                     [(denorm-fn id options) amount]))
                              (into {})))
    (dissoc recipe :recipe/input)))

(defn denormalize
  ([id] (denormalize id {}))

  ([id {:keys [all-items all-recipes]
        :or {all-items items/all
             all-recipes recipes/all}
        :as options}]
   (cond
     (get all-items id)
     (-> id
         (item-id->item all-items)
         (denorm-item denormalize options))

     (get all-recipes id)
     (-> id
         (recipe-id->recipe all-recipes)
         (denorm-recipe denormalize options))

     :else
     (throw (js/Error. (str "No Item or Recipe of ID " id " found."))))))
