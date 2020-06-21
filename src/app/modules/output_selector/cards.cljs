(ns app.modules.output-selector.cards
  (:require [devcards.core :as dc :refer [defcard deftest]]
            [cljs.test :include-macros true :refer [is]]
            [app.modules.output-selector.components :refer [output-selector]]
            [app.recipes :as recipes]))

(defn testing-container
  "The container that should be used to render testing-library react components.
  We want to provide our own container so that the rendered devcards aren't used."
  []
  (let [app-div (js/document.createElement "div")]
    (.setAttribute app-div "id" "testing-lib")
    (js/document.body.appendChild app-div)))

(defcard output-selector-card
  (dc/reagent #(output-selector {:recipes recipes/all})))
