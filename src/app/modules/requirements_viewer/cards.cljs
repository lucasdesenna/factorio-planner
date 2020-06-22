(ns app.modules.requirements-viewer.cards
  (:require [devcards.core :as dc :refer [defcard deftest]]
            [cljs.test :include-macros true :refer [are testing]]
            [app.common.denormalize.logic :as c.d.l]
            [app.modules.requirements-viewer.components :refer [requirements-viewer]]
            [app.modules.requirements-viewer.logic :as fac-pre.l]
            [app.items :as items]
            [app.recipes :as recipes]))

(defn testing-container
  "The container that should be used to render testing-library react components.
  We want to provide our own container so that the rendered devcards aren't used."
  []
  (let [app-div (js/document.createElement "div")]
    (.setAttribute app-div "id" "testing-lib")
    (js/document.body.appendChild app-div)))

(defcard requirements-viewer-card
  (dc/reagent #(requirements-viewer {:factory-input [:item.iron-plate]
                                     :factory-input-amount 1
                                     :factory-output :item.iron-gear-wheel
                                     :all-items items/all
                                     :all-recipes recipes/all})))

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

(deftest denormalized-item->requirements
  (testing "Calculates production requirements of given a denormalized item"
    (are [item-id requirements] (= (-> item-id
                                       (c.d.l/denormalize {:all-items all-items
                                                           :all-recipes all-recipes})
                                       fac-pre.l/denormalized-item->requirements)
                                   requirements)
      :item.raw-a {}
      :item.a {:recipe.a {:assemblers 0.5
                          :consumption-per-sec {:item.raw-a 0.5}}}
      :item.b {:recipe.a {:assemblers 4
                          :consumption-per-sec {:item.raw-a 4}}
               :recipe.b {:assemblers 2
                          :consumption-per-sec {:item.a 8}}}
      :item.c {:recipe.a {:assemblers 4.5
                          :consumption-per-sec {:item.raw-a 4.5}}
               :recipe.b {:assemblers 2
                          :consumption-per-sec {:item.a 8}}
               :recipe.c {:assemblers 4
                          :consumption-per-sec {:item.a 1
                                                :item.b 1}}})))
