(ns app.modules.requirements-viewer.logic)

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
   (if-let [produced-by (:item/produced-by item)]
     (let [{:recipe/keys [id time input output]} produced-by
           output-per-sec (-> output
                              item-id
                              (/ time))
           req-assemblers (/ req-output-per-sec output-per-sec)
           consumption-per-sec (->> input
                                    (map (fn [[{:item/keys [id]} amount]] [id (/ (* amount req-assemblers) time)]))
                                    (into {}))]
       (reduce (fn [acc [item' amount]]
                 (let [req-output-per-sec (/ (* amount req-assemblers) time)]
                   (merge-requirements acc (denormalized-item->requirements item' req-output-per-sec))))
               {id {:assemblers req-assemblers
                    :consumption-per-sec consumption-per-sec}}
               input))
     {})))
