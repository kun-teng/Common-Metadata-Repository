(ns cmr.indexer.data.concepts.service
  "Contains functions to parse and convert service and service association concepts."
  (:require
   [cmr.common.log :refer (debug info warn error)]
   [cmr.indexer.data.concept-parser :as concept-parser]
   [cmr.transmit.metadata-db :as mdb]))

(defn- service-association->service-concept
  "Returns the service concept and service association for the given service association."
  [context service-association]
  (let [{:keys [service-concept-id]} service-association
        service-concept (mdb/find-latest-concept
                         context
                         {:concept-id service-concept-id}
                         :service)]
    (when-not (:deleted service-concept)
      service-concept)))

(defn- has-formats?
  "Returns true if the given service has more than one SupportedFormats value."
  [context service-concept]
  (let [service (concept-parser/parse-concept context service-concept)
        supported-formats (get-in service [:ServiceOptions :SupportedFormats])]
    (> (count supported-formats) 1)))

(defn- has-spatial-subsetting?
  "Returns true if the given service has a defined SubsetType with one of its
  values being 'spatial'."
  [context service-concept]
  (let [service (concept-parser/parse-concept context service-concept)
        {{subset-type :SubsetType} :ServiceOptions} service]
    (and (seq subset-type)
         (contains? (set subset-type) "Spatial"))))

(defn- has-transforms?
  "Returns true if the given service has a defined SubsetType or InterpolationType,
  or multiple SupportedProjections values."
  [context service-concept]
  (let [service (concept-parser/parse-concept context service-concept)
        {service-options :ServiceOptions} service
        {subset-type :SubsetType
         interpolation-type :InterpolationType
         supported-projections :SupportedProjections} service-options]
    (or (seq subset-type)
        (seq interpolation-type)
        (> (count supported-projections) 1))))

(defn service-associations->elastic-doc
  "Converts the service association into the portion going in the collection elastic document."
  [context service-associations]
  (let [service-concepts (remove nil?
                                 (map #(service-association->service-concept context %)
                                      service-associations))]
    {:has-formats (boolean (some #(has-formats? context %) service-concepts))
     :has-spatial-subsetting (boolean (some #(has-spatial-subsetting? context %) service-concepts))
     :has-transforms (boolean (some #(has-transforms? context %) service-concepts))}))
