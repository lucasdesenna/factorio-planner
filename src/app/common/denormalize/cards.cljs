(ns app.common.denormalize.cards
  (:require [devcards.core :as dc :refer [defcard deftest]]
            [cljs.test :include-macros true :refer [is are testing]]
            [app.common.denormalize.logic :as logic]))

(defn testing-container
  "The container that should be used to render testing-library react components.
  We want to provide our own container so that the rendered devcards aren't used."
  []
  (let [app-div (js/document.createElement "div")]
    (.setAttribute app-div "id" "testing-lib")
    (js/document.body.appendChild app-div)))

(def all-items
  {:item.raw-a {:item/id :item.raw-a
                :item/name "Raw A"
                :item/consumed-by [:recipe.a]}
   :item.a {:item/id :item.a
            :item/name "A"
            :item/consumed-by [:recipe.b :recipe.c]
            :item/produced-by [:recipe.a]}
   :item.b {:item/id :item.b
            :item/name "B"
            :item/consumed-by [:recipe.c]
            :item/produced-by [:recipe.b]}
   :item.c {:item/id :item.c
            :item/name "C"
            :item/consumed-by []
            :item/produced-by [:recipe.c]}})

(def item-raw-a (:item.raw-a all-items))

(def item-a (:item.a all-items))

(def item-b (:item.b all-items))

(def item-c (:item.c all-items))

(deftest item-id->item
  (is (thrown? js/Error (logic/item-id->item :non-existing-recipe-id all-items)))
  (are [id item] (= (logic/item-id->item id all-items) item)
    :item.a item-a
    :item.b item-b
    :item.c item-c))

(def all-recipes
  {:recipe.a {:recipe/id :recipe.a
              :recipe/name "A"
              :recipe/time 1
              :recipe/input {:item.raw-a 1}
              :recipe/output {:item.a 2}}
   :recipe.b {:recipe/id :recipe.b
              :recipe/name "B"
              :recipe/time 2
              :recipe/input {:item.a 8}
              :recipe/output {:item.b 1}}
   :recipe.c {:recipe/id :recipe.c
              :recipe/name "C"
              :recipe/time 4
              :recipe/input {:item.a 1
                             :item.b 1}
              :recipe/output {:item.c 1}}})

(def recipe-a (:recipe.a all-recipes))

(def recipe-b (:recipe.b all-recipes))

(def recipe-c (:recipe.c all-recipes))

(deftest recipe-id->recipe
  (is (thrown? js/Error (logic/recipe-id->recipe :non-existing-recipe-id all-recipes)))
  (are [id recipe] (= (logic/recipe-id->recipe id all-recipes) recipe)
    :recipe.a recipe-a
    :recipe.b recipe-b
    :recipe.c recipe-c))

(def denormalized-recipe-a (assoc recipe-a :recipe/input {item-raw-a 1}))

(def denormalized-item-a (assoc item-a :item/produced-by denormalized-recipe-a))

(def denormalized-recipe-b (assoc recipe-b :recipe/input {denormalized-item-a 8}))

(def denormalized-item-b (assoc item-b :item/produced-by denormalized-recipe-b))

(def denormalized-recipe-c (assoc recipe-c :recipe/input {denormalized-item-a 1
                                                          denormalized-item-b 1}))

(def denormalized-item-c (assoc item-c :item/produced-by denormalized-recipe-c))

(def denormalized-recipe-a-wo-input (assoc denormalized-recipe-a :recipe/input {}))

(def denormalized-item-a-wo-produced-by (dissoc denormalized-item-a :item/produced-by))

(def denormalized-recipe-b-wo-input (assoc denormalized-recipe-b :recipe/input {}))

(def denormalized-item-b-wo-recipe-a (assoc-in denormalized-item-b
                                               [:item/produced-by :recipe/input]
                                               {(dissoc denormalized-item-a :item/produced-by) 8}))

(def denormalized-recipe-c-wo-input (assoc denormalized-recipe-c :recipe/input {}))

(def denormalized-recipe-c-wo-item-b (assoc denormalized-recipe-c :recipe/input {denormalized-item-a 1}))

(def denormalized-item-c-wo-recipe-b (assoc-in denormalized-item-c
                                               [:item/produced-by :recipe/input]
                                               {denormalized-item-a 1
                                                (dissoc denormalized-item-b :item/produced-by) 1}))

(def denormalized-item-c-wo-recipe-a (assoc-in denormalized-item-c
                                               [:item/produced-by :recipe/input]
                                               {(dissoc denormalized-item-a :item/produced-by) 1
                                                denormalized-item-b 1}))

(def denormalized-item-c-wo-recipe-a-and-recipe-b (assoc-in denormalized-item-c
                                                            [:item/produced-by :recipe/input]
                                                            {(dissoc denormalized-item-a :item/produced-by) 1
                                                             (dissoc denormalized-item-b :item/produced-by) 1}))

(deftest denormalize
  (testing "Denormalizes a given item-id"
    (are [item-id denormalized] (= (logic/denormalize item-id {:ignored-ids #{}
                                                               :all-items all-items
                                                               :all-recipes all-recipes}) denormalized)
      :item.a denormalized-item-a
      :item.b denormalized-item-b
      :item.c denormalized-item-c))

  (testing "Denormalizes a given recipe-id"
    (are [recipe-id denormalized] (= (logic/denormalize recipe-id {:ignored-ids #{}
                                                                   :all-items all-items
                                                                   :all-recipes all-recipes}) denormalized)
      :recipe.a denormalized-recipe-a
      :recipe.b denormalized-recipe-b
      :recipe.c denormalized-recipe-c))

  (testing "Ignores items and recipes which ids are contained in the ignored-id set"
    (are [id ignored-ids denormalized] (= (logic/denormalize id {:ignored-ids ignored-ids
                                                                 :all-items all-items
                                                                 :all-recipes all-recipes}) denormalized)
      :item.a #{} denormalized-item-a
      :recipe.a #{} denormalized-recipe-a
      :recipe.a #{:item.raw-a} denormalized-recipe-a-wo-input
      :item.b #{:recipe.a} denormalized-item-b-wo-recipe-a
      :recipe.b #{:item.a} denormalized-recipe-b-wo-input
      :item.c #{:recipe.b} denormalized-item-c-wo-recipe-b
      :item.c #{:recipe.a :recipe.b} denormalized-item-c-wo-recipe-a-and-recipe-b
      :recipe.c #{:item.b} denormalized-recipe-c-wo-item-b
      :recipe.c #{:item.a :item.b} denormalized-recipe-c-wo-input)))
