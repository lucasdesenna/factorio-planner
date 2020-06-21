(ns app.modules.factory-previewer.logic
  (:require [app.recipes :as recipes]
            [app.items :as items]))

(defn item-id->item
  ([id] (item-id->item id recipes/all))

  ([id all-recipes]
   (if-let [recipe (get all-recipes id)]
     recipe
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
  (if-let [main-producer (some-> (remove (partial contains? ignored-ids) produced-by)
                                 first
                                 (denorm-fn options))]
    (assoc item
           :item/produced-by main-producer)
    (dissoc item :item/produced-by)))

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

(defn- merge-requirements [r1 r2]
  (reduce-kv
   (fn [acc recipe-id {:keys [assemblers consumption-per-sec]}]
     (-> acc
         (update-in [recipe-id :assemblers] + assemblers)
         (update-in [recipe-id :consumption-per-sec] (partial merge-with + {}) consumption-per-sec)))
   r1
   r2))

(defn denormalized-item->requirements
  ([item] (denormalized-item->requirements item 1))

  ([{item-id :item/id :as item} req-output-per-sec]
   (let [{:recipe/keys [id time input output]} (:item/produced-by item)
         output-per-sec (-> output
                            item-id
                            (/ time))
         req-assemblers (/ req-output-per-sec output-per-sec)
         consumption-per-sec (->> input
                                  (map (fn [[{:item/keys [id]} amount]] [id (/ (* amount req-assemblers) time)]))
                                  (into {}))]
     (reduce (fn [acc [item' amount]]
               (if (:item/produced-by item')
                 (let [req-output-per-sec (/ (* amount req-assemblers) time)]
                   (merge-requirements acc (denormalized-item->requirements item' req-output-per-sec)))
                 acc))
             {id {:assemblers req-assemblers
                  :consumption-per-sec consumption-per-sec}}
             input))))
