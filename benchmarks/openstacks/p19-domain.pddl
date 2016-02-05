(define (domain openstacks-sequencedstrips-nonADL-nonNegated)
(:requirements :typing :action-costs)
(:types order product count manager-agent manufacturer-agent)
(:constants 
 p1 p2 p3 p4 p5 p6 p7 p8 p9 p10 p11 p12 p13 p14 p15 p16 p17 p18 p19 p20 p21 p22 p23 p24 p25 p26 p27 p28 p29 p30 p31 p32 p33 p34 p35 p36 p37 p38 p39 p40 p41 p42 p43 p44 p45 p46 p47 p48 p49 p50 - product
 o1 o2 o3 o4 o5 o6 o7 o8 o9 o10 o11 o12 o13 o14 o15 o16 o17 o18 o19 o20 o21 o22 o23 o24 o25 o26 o27 o28 o29 o30 o31 o32 o33 o34 o35 o36 o37 o38 o39 o40 o41 o42 o43 o44 o45 o46 o47 o48 o49 o50 - order
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
:precondition (and (not-made p1)(started o18)(started o46))
:effect (and (not (not-made p1)) (made p1))
)

(:action make-product-p2
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p2)(started o5))
:effect (and (not (not-made p2)) (made p2))
)

(:action make-product-p3
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p3)(started o8))
:effect (and (not (not-made p3)) (made p3))
)

(:action make-product-p4
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p4)(started o7)(started o14)(started o42))
:effect (and (not (not-made p4)) (made p4))
)

(:action make-product-p5
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p5)(started o8)(started o21))
:effect (and (not (not-made p5)) (made p5))
)

(:action make-product-p6
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p6)(started o1)(started o7)(started o43)(started o47))
:effect (and (not (not-made p6)) (made p6))
)

(:action make-product-p7
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p7)(started o22))
:effect (and (not (not-made p7)) (made p7))
)

(:action make-product-p8
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p8)(started o7))
:effect (and (not (not-made p8)) (made p8))
)

(:action make-product-p9
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p9)(started o5)(started o19)(started o43))
:effect (and (not (not-made p9)) (made p9))
)

(:action make-product-p10
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p10)(started o19)(started o25)(started o41))
:effect (and (not (not-made p10)) (made p10))
)

(:action make-product-p11
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p11)(started o10)(started o19)(started o35)(started o39))
:effect (and (not (not-made p11)) (made p11))
)

(:action make-product-p12
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p12)(started o10)(started o35))
:effect (and (not (not-made p12)) (made p12))
)

(:action make-product-p13
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p13)(started o5)(started o6)(started o36)(started o42))
:effect (and (not (not-made p13)) (made p13))
)

(:action make-product-p14
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p14)(started o38))
:effect (and (not (not-made p14)) (made p14))
)

(:action make-product-p15
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p15)(started o10)(started o33)(started o36)(started o40))
:effect (and (not (not-made p15)) (made p15))
)

(:action make-product-p16
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p16)(started o48))
:effect (and (not (not-made p16)) (made p16))
)

(:action make-product-p17
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p17)(started o43))
:effect (and (not (not-made p17)) (made p17))
)

(:action make-product-p18
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p18)(started o50))
:effect (and (not (not-made p18)) (made p18))
)

(:action make-product-p19
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p19)(started o5)(started o21)(started o29))
:effect (and (not (not-made p19)) (made p19))
)

(:action make-product-p20
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p20)(started o28))
:effect (and (not (not-made p20)) (made p20))
)

(:action make-product-p21
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p21)(started o8))
:effect (and (not (not-made p21)) (made p21))
)

(:action make-product-p22
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p22)(started o23)(started o27)(started o29))
:effect (and (not (not-made p22)) (made p22))
)

(:action make-product-p23
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p23)(started o2)(started o20))
:effect (and (not (not-made p23)) (made p23))
)

(:action make-product-p24
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p24)(started o12)(started o15)(started o20)(started o29)(started o37)(started o49))
:effect (and (not (not-made p24)) (made p24))
)

(:action make-product-p25
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p25)(started o21)(started o23)(started o31)(started o44))
:effect (and (not (not-made p25)) (made p25))
)

(:action make-product-p26
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p26)(started o11)(started o31))
:effect (and (not (not-made p26)) (made p26))
)

(:action make-product-p27
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p27)(started o8)(started o41))
:effect (and (not (not-made p27)) (made p27))
)

(:action make-product-p28
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p28)(started o7)(started o14)(started o15)(started o34)(started o49))
:effect (and (not (not-made p28)) (made p28))
)

(:action make-product-p29
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p29)(started o4))
:effect (and (not (not-made p29)) (made p29))
)

(:action make-product-p30
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p30)(started o43))
:effect (and (not (not-made p30)) (made p30))
)

(:action make-product-p31
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p31)(started o1)(started o12)(started o26)(started o33))
:effect (and (not (not-made p31)) (made p31))
)

(:action make-product-p32
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p32)(started o17)(started o24))
:effect (and (not (not-made p32)) (made p32))
)

(:action make-product-p33
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p33)(started o6)(started o48))
:effect (and (not (not-made p33)) (made p33))
)

(:action make-product-p34
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p34)(started o6)(started o49))
:effect (and (not (not-made p34)) (made p34))
)

(:action make-product-p35
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p35)(started o3)(started o32))
:effect (and (not (not-made p35)) (made p35))
)

(:action make-product-p36
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p36)(started o2)(started o12))
:effect (and (not (not-made p36)) (made p36))
)

(:action make-product-p37
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p37)(started o33)(started o40)(started o43))
:effect (and (not (not-made p37)) (made p37))
)

(:action make-product-p38
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p38)(started o4)(started o29))
:effect (and (not (not-made p38)) (made p38))
)

(:action make-product-p39
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p39)(started o6))
:effect (and (not (not-made p39)) (made p39))
)

(:action make-product-p40
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p40)(started o18))
:effect (and (not (not-made p40)) (made p40))
)

(:action make-product-p41
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p41)(started o30)(started o33))
:effect (and (not (not-made p41)) (made p41))
)

(:action make-product-p42
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p42)(started o16))
:effect (and (not (not-made p42)) (made p42))
)

(:action make-product-p43
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p43)(started o4)(started o12)(started o18))
:effect (and (not (not-made p43)) (made p43))
)

(:action make-product-p44
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p44)(started o4)(started o42))
:effect (and (not (not-made p44)) (made p44))
)

(:action make-product-p45
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p45)(started o30))
:effect (and (not (not-made p45)) (made p45))
)

(:action make-product-p46
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p46)(started o12))
:effect (and (not (not-made p46)) (made p46))
)

(:action make-product-p47
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p47)(started o13)(started o18))
:effect (and (not (not-made p47)) (made p47))
)

(:action make-product-p48
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p48)(started o12)(started o13)(started o27)(started o37))
:effect (and (not (not-made p48)) (made p48))
)

(:action make-product-p49
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p49)(started o9))
:effect (and (not (not-made p49)) (made p49))
)

(:action make-product-p50
:parameters (?man - manufacturer-agent)
:precondition (and (not-made p50)(started o45))
:effect (and (not (not-made p50)) (made p50))
)

(:action ship-order-o1
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o1)(made p6)(made p31)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o1))(shipped o1)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o2
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o2)(made p23)(made p36)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o2))(shipped o2)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o3
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o3)(made p35)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o3))(shipped o3)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o4
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o4)(made p29)(made p38)(made p43)(made p44)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o4))(shipped o4)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o5
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o5)(made p2)(made p9)(made p13)(made p19)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o5))(shipped o5)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o6
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o6)(made p13)(made p33)(made p34)(made p39)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o6))(shipped o6)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o7
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o7)(made p4)(made p6)(made p8)(made p28)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o7))(shipped o7)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o8
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o8)(made p3)(made p5)(made p21)(made p27)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o8))(shipped o8)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o9
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o9)(made p49)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o9))(shipped o9)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o10
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o10)(made p11)(made p12)(made p15)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o10))(shipped o10)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o11
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o11)(made p26)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o11))(shipped o11)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o12
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o12)(made p24)(made p31)(made p36)(made p43)(made p46)(made p48)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o12))(shipped o12)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o13
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o13)(made p47)(made p48)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o13))(shipped o13)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o14
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o14)(made p4)(made p28)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o14))(shipped o14)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o15
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o15)(made p24)(made p28)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o15))(shipped o15)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o16
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o16)(made p42)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o16))(shipped o16)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o17
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o17)(made p32)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o17))(shipped o17)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o18
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o18)(made p1)(made p40)(made p43)(made p47)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o18))(shipped o18)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o19
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o19)(made p9)(made p10)(made p11)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o19))(shipped o19)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o20
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o20)(made p23)(made p24)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o20))(shipped o20)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o21
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o21)(made p5)(made p19)(made p25)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o21))(shipped o21)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o22
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o22)(made p7)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o22))(shipped o22)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o23
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o23)(made p22)(made p25)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o23))(shipped o23)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o24
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o24)(made p32)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o24))(shipped o24)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o25
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o25)(made p10)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o25))(shipped o25)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o26
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o26)(made p31)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o26))(shipped o26)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o27
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o27)(made p22)(made p48)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o27))(shipped o27)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o28
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o28)(made p20)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o28))(shipped o28)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o29
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o29)(made p19)(made p22)(made p24)(made p38)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o29))(shipped o29)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o30
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o30)(made p41)(made p45)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o30))(shipped o30)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o31
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o31)(made p25)(made p26)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o31))(shipped o31)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o32
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o32)(made p35)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o32))(shipped o32)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o33
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o33)(made p15)(made p31)(made p37)(made p41)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o33))(shipped o33)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o34
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o34)(made p28)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o34))(shipped o34)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o35
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o35)(made p11)(made p12)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o35))(shipped o35)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o36
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o36)(made p13)(made p15)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o36))(shipped o36)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o37
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o37)(made p24)(made p48)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o37))(shipped o37)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o38
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o38)(made p14)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o38))(shipped o38)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o39
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o39)(made p11)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o39))(shipped o39)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o40
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o40)(made p15)(made p37)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o40))(shipped o40)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o41
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o41)(made p10)(made p27)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o41))(shipped o41)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o42
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o42)(made p4)(made p13)(made p44)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o42))(shipped o42)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o43
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o43)(made p6)(made p9)(made p17)(made p30)(made p37)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o43))(shipped o43)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o44
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o44)(made p25)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o44))(shipped o44)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o45
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o45)(made p50)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o45))(shipped o45)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o46
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o46)(made p1)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o46))(shipped o46)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o47
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o47)(made p6)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o47))(shipped o47)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o48
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o48)(made p16)(made p33)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o48))(shipped o48)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o49
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o49)(made p24)(made p28)(made p34)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o49))(shipped o49)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

(:action ship-order-o50
:parameters (?man - manager-agent ?avail ?new-avail - count)
:precondition (and (started o50)(made p18)(stacks-avail ?avail)(next-count ?avail ?new-avail))
:effect (and (not (started o50))(shipped o50)(not (stacks-avail ?avail))(stacks-avail ?new-avail)))

)

