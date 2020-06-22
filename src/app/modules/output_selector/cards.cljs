(ns app.modules.output-selector.cards
  (:require [devcards.core :as dc :refer [defcard]]
            [app.modules.output-selector.components :refer [output-selector]]
            [app.items :as items]
            [reagent.core :as r]))

(defn testing-container
  "The container that should be used to render testing-library react components.
  We want to provide our own container so that the rendered devcards aren't used."
  []
  (let [app-div (js/document.createElement "div")]
    (.setAttribute app-div "id" "testing-lib")
    (js/document.body.appendChild app-div)))

(defcard output-selector-card
  (let [state (r/atom {:items items/all
                       :output-id nil
                       :output-amount 1})
        on-ouput-id-change #(swap! state assoc :factory/output-id %)
        on-ouput-amount-change #(swap! state assoc :factory/output-amount %)]
    (dc/reagent #(output-selector (merge @state
                                         {:on-ouput-id-change on-ouput-id-change
                                          :on-ouput-amount-change on-ouput-amount-change})))))
