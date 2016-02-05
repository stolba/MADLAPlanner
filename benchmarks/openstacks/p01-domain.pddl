(define (domain openstacks-sequencedstrips-nonADL-nonNegated)
(:requirements :typing :action-costs)
(:types order product count manager-agent manufacturer-agent)
(:constants 
 p1 p2 p3 p4 p5 - product
 o1 o2 o3 o4 o5 - order
 manager - manager-agent
 manufacturer - manufacturer-agent
)

(:predicates 
	(includes ?o - order ?p - product)
	(waiting ?o - order)
	(started ?o - order)
	(shipped ?o - order)
	(made ?p - product)
	(not-made ?p - product)
	(stacks-avail ?s - count)
	(next-count ?s ?ns - count)
)

(:functions
(total-cost) - number
)

(:action open-new-stack
:parameters (?man - manufacturer-agent ?open ?new-open - count)
:precondition (and (stacks-avail ?open)(next-count ?open ?new-open))
:effect (and (not (stacks-avail ?open))(stacks-avail ?new-open) (increase (total-cost) 1))
)

(:action start-order
:parameters (?man - manager-agent ?o - order ?avail ?new-avail - count)
:precondition (and (waiting ?o)(stacks-avail ?avail)(next-count ?new-avail ?avail))
:effect (and (not (waiting ?o))(started ?o)(not (stacks-avail ?avail))(stacks-avail ?new-avail))
)

(:action make-product-p1
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p1)(started o2))
:effect (and (not (not-made p1)) (made p1))
)

(:action make-product-p2
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p2)(started o1)(started o2))
:effect (and (not (not-made p2)) (made p2))
)

(:action make-product-p3
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p3)(started o3)(started o4))
:effect (and (not (not-made p3)) (made p3))
)

(:action make-product-p4
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p4)(started o4))
:effect (and (not (not-made p4)) (made p4))
)

(:action make-product-p5
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p5)(started o5))
:effect (and (not (not-made p5)) (made p5))
)

(:action ship-order-o1
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o1)(made p2)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o1))(shipped o1)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o2
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o2)(made p1)(made p2)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o2))(shipped o2)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o3
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o3)(made p3)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o3))(shipped o3)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o4
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o4)(made p3)(made p4)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o4))(shipped o4)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o5
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o5)(made p5)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o5))(shipped o5)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

)

