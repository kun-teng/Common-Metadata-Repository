(ns cmr.umm-spec.test.versioning
  (:require [cmr.umm-spec.versioning :as v]
            [clojure.test :refer :all]))

(deftest migrate-1_0-up-to-1_1
  (is (nil?
        (:TilingIdentificationSystems
          (v/migrate-umm {:TilingIdentificationSystem nil}
                         :collection "1.0" "1.1"))))
  (is (= [{:TilingIdentificationSystemName "foo"}]
         (:TilingIdentificationSystems
           (v/migrate-umm {:TilingIdentificationSystem {:TilingIdentificationSystemName "foo"}}
                          :collection "1.0" "1.1")))))

(deftest migrate-1_1-down-to-1_0
  (is (nil?
        (:TilingIdentificationSystem
          (v/migrate-umm {:TilingIdentificationSystems nil}
                         :collection "1.1" "1.0"))))
  (is (nil?
        (:TilingIdentificationSystem
          (v/migrate-umm {:TilingIdentificationSystems []}
                         :collection "1.1" "1.0"))))
  (is (= {:TilingIdentificationSystemName "foo"}
         (:TilingIdentificationSystem
           (v/migrate-umm {:TilingIdentificationSystems [{:TilingIdentificationSystemName "foo"}
                                                         {:TilingIdentificationSystemName "bar"}]}
                          :collection "1.1" "1.0")))))