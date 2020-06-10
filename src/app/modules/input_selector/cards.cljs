(ns app.modules.input-selector.cards
  (:require [devcards.core :as dc :refer [defcard deftest]]
            [cljs.test :include-macros true :refer [is]]
            [app.modules.input-selector.components :refer [input-selector]]
            [app.items :as items]))

(defn testing-container
  "The container that should be used to render testing-library react components.
  We want to provide our own container so that the rendered devcards aren't used."
  []
  (let [app-div (js/document.createElement "div")]
    (.setAttribute app-div "id" "testing-lib")
    (js/document.body.appendChild app-div)))

(defcard input-selector-card
  (dc/reagent #(input-selector {:items items/all})))