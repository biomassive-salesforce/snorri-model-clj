(ns snorri-model.process)

(defn filter-outliers
  "Remove outliers from the list."
  [lower upper l]
  (filter #(< lower % upper) l))

(defn average
  "Calculate the average of the given numbers."
  [l]
  (if-not (empty? l)
    (let [sum (apply + l)]
      (.divide (bigdec sum) (bigdec (count l)) java.math.BigDecimal/ROUND_HALF_UP))))

(defn round
  "Round the number to 2 digits"
  [n]
  (Double/parseDouble (format "%.2f" n)))

(def pe-min 7)
(def pe-max 32)

(defn calc-avg-pe [l]
  (round (average (filter-outliers pe-min pe-max l))))

(defn calc-sum-es [l]
  (round (apply + l)))

(defn calc-safe-eg [eg]
  (round (if (< eg 10)
    (dec eg)
    (- eg (quot eg 4)))))

(defn calc-exp [pe es eg]
  (round (* es pe (Math/pow (inc (/ eg 100)) 5))))

(defn calc-gain [close exp]
  (if (pos? close)
    (round (* 100 (dec (Math/pow (/ exp close) 0.2))))
    0.0))

(defn give-advise [gain]
  (cond
    (>= gain 20) "BUY"
    (<= gain 8) "SELL"
    :else "HOLD"))

(defn enrich-data [{:keys [close pe es eg] :as data}]
  (let [avg-pe (calc-avg-pe pe)
        sum-es (calc-sum-es es)
        safe-eg (calc-safe-eg eg)
        exp (calc-exp avg-pe sum-es safe-eg)
        gain (calc-gain close exp)
        advise (give-advise gain)]
    (assoc data :avg-pe avg-pe :sum-es sum-es :safe-eg safe-eg :exp exp :gain gain :advise advise)))
