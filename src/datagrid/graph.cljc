(ns datagrid.graph)

(def rules
  [{:inputs  [:a :b]
    :outputs [:c]
    :handler (fn [ctx [a b] [c]]
               [c])}
   {:inputs  [:c]
    :outputs [:d :e]
    :handler (fn [ctx [a b] [d e]]
               [d e])}
   {:inputs  [:g]
    :outputs [:h]
    :handler (fn [ctx [c g] [h]]
               [h])}
   {:inputs  [:d]
    :outputs [:f]
    :handler (fn [ctx [c d] [f]]
               [f])}])

(def rules1
  [{:inputs  [:a]
    :outputs [:b]
    #_#_:handler (fn [ctx [a] [b]]
                   [b])}
   {:inputs  [:b]
    :outputs [:a]
    #_#_:handler (fn [ctx [b] [a]]
                   [a])}
   {:inputs  [:b]
    :outputs [:c]
    #_#_:handler (fn [ctx [b] [a]]
                   [a])}])

(def rules2
  [{:inputs  [:a]
    :outputs [:b]
    #_#_:handler (fn [ctx [a] [b]]
                   [b])}
   {:inputs  [:b]
    :outputs [:a]
    #_#_:handler (fn [ctx [b] [a]]
                   [a])}
   {:inputs  [:b]
    :outputs [:c]
    #_#_:handler (fn [ctx [b] [a]]
                   [a])}
   {:inputs  [:c]
    :outputs [:d]
    #_#_:handler (fn [ctx [b] [a]]
                   [a])}

   {:inputs  [:d]
    :outputs [:e]
    #_#_:handler (fn [ctx [b] [a]]
                   [a])}])

(def rules3
  [{:inputs  [:a]
    :outputs [:a :b]}
   {:inputs  [:b]
    :outputs [:b :a]}])

(def rules4
  [{:inputs  []
    :outputs []}
   {:inputs  [:a]
    :outputs [:b]}])

(defn find-related
  [input nodes]
  (->> nodes
       (keep (fn [{:keys [inputs outputs]}]
               (when (some #{input} outputs)
                 (remove #(= input %) inputs))))
       (apply concat)))

(def conj-set (fnil conj #{}))
(def into-set (fnil into #{}))

(defn add-nodes
  [ctx inputs]
  (reduce
    (fn [[{:keys [nodes] :as ctx} inputs] input]
      (let [related (find-related input nodes)]
        [(-> ctx
             (update :visited conj-set input)
             (update :graph update input into-set related))
         (into inputs related)]))
    [ctx #{}]
    inputs))

(defn base-graph-ctx
  [nodes]
  {:nodes nodes
   :graph {}
   :steps 0})

(defn input-nodes
  [rules]
  (distinct (mapcat :inputs rules)))

(defn connect
  ([nodes]
   (connect (base-graph-ctx nodes)
            (input-nodes nodes)))
  ([ctx inputs]
   (let [[{:keys [steps visited graph] :as ctx} inputs] (add-nodes ctx inputs)]
     (if (and (not-empty inputs) (< steps 5))
       (do
         (println graph inputs)
         (recur (update ctx :steps inc)
                (remove #(some #{%} visited) inputs)))
       graph))))

(comment

  (connect rules2)

  (find-related :a rules1)

  (-> (add-nodes {:nodes rules1 :graph {}} [:a]) first :visited)

  )
