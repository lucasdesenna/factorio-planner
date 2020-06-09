(ns raw-data-parser.parse-test
  (:require [cljs.test :include-macros true :refer [deftest is are testing]]
            [raw-data-parser.parse :as parse]))

(deftest name->id
  (is (thrown? js/Error (parse/name->id "")))
  (are [n id] (= (parse/name->id n) id)
    "Name" :name
    "Compound name" :compound-name
    "Long compound name" :long-compound-name))

(deftest id->name
  (are [id n] (= (parse/id->name id) n)
    :name "Name"
    :compound-name "Compound name"
    :long-compound-name "Long compound name"))
