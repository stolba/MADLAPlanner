(ns trace-check
  (:use [clojure.pprint])
  (:use [clojure.core.match :only (match)]))

; 0          1        2        3      4       5              6         7                     8
; :send      sender   receiver :REQ   reqCode recursionDepth stateCode waitingForRepliesNo
; :receive1  receiver sender   :REQ   reqCode recursionDepth stateCode waitingForRepliesNo-1 heuristicValue+resultValue
; :receive0  receiver sender   :REQ   reqCode recursionDepth stateCode waitingForRepliesNo-1 heuristicValue
; :increase  agent    nil      :REQ   reqCode recursionDepth stateCode waitingForRepliesNo
; :decrease  agent    nil      :REQ   reqCode recursionDepth stateCode waitingForRepliesNo-1
; :error-DNE agent    nil      :REQ   reqCode recursionDepth stateCode

; :receive   receiver sender   :REP   reqCode recursionDepth stateCode

; :send      sender   receiver :REPPR reqCode recursionDepth stateCode nil                   heuristicValue


(def data (read-string (str "(" (slurp "log/trace.log") ")")))

(def traces (map
              (fn [[key val]] [key (group-by #(nth % 4) val)]) ; 4 = reqCode
              (group-by #(nth % 6) data)))                    ; 6 = stateCode
;(binding [*print-right-margin* 120] (pprint traces))


(defn check [c]
  (match [c]

          [[[:increase from0 nil :REQ _ _ _ 1]
            [:send from1 to1 :REQ _ _ _ _]
            [:receive to2 from2 :REP _ _ _]
            [:send to3 from3 :REPPR _ _ _ nil h3]
            [:receive0 from4 to4 :REQ _ _ _ _ h4]
            [:decrease from5 nil :REQ _ _ _ 0]]]
                (if (and
                      (= from0 from1 from2 from3 from4 from5)
                      (= to1 to2 to3 to4)
                      (= h3 h4)) :ok [:error-eq c])

          [[[:increase from0 nil :REQ _ _ _ 1]
            [:send from1 to1 :REQ _ _ _ _]
            [:receive to2 from2 :REP _ _ _]
            [:send to3 from3 :REPPR _ _ _ nil h3]
            [:increase from4 nil :REQ _ _ _ 2]
            [:decrease from5 nil :REQ _ _ _ 1]
            [:receive0 from6 to6 :REQ _ _ _ _ h6]
            [:decrease from7 nil :REQ _ _ _ 0]]]
                (if (and
                      (= from0 from1 from2 from3 from4 from5 from6 from7)
                      (= to1 to2 to3 to6)
                      (= h3 h6)) :ok [:error-eq c])

          [[[:increase from0 nil :REQ _ _ _ 1]
            [:send from1 to1 :REQ _ _ _ _]
            [:receive to2 from2 :REP _ _ _]
            [:send to3 from3 :REPPR _ _ _ nil h3]
            [:increase from4 nil :REQ _ _ _ 2]
            [:receive1 from5 to5 :REQ _ _ _ _ _]
            [:decrease from6 nil :REQ _ _ _ 1]
            [:receive0 from7 to7 :REQ _ _ _ _ h7]
            [:decrease from8 nil :REQ _ _ _ 0]]]
                (if (and
                      (= from0 from1 from2 from3 from4 from5 from6 from7 from8 to5)
                      (= to1 to2 to3 to7)
                      (= h3 h7)) :ok [:error-eq c])

          [[[:increase from0 nil :REQ _ _ _ 1]
            [:send from1 to1 :REQ _ _ _ _]
            [:receive to2 from2 :REP _ _ _]
            [:increase from3 nil :REQ _ _ _ 2]
            [:decrease from4 nil :REQ _ _ _ 1]
            [:receive0 from5 to6 :REQ _ _ _ _ _]
            [:decrease from6 nil :REQ _ _ _ 0]]]
                (if (= from0 from1 from2 from3 from4 from5 from6 to1 to2 to6) :ok [:error-eq c])

          [_] [:error-match c]))

(pprint (map
          (fn [[key val]] (map
                            (fn [[key val]] (check val))
                            val))
          traces))
