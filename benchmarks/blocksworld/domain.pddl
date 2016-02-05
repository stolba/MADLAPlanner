;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; 4 Op-blocks world
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(define (domain BLOCKS)
  (:requirements :strips)
  (:predicates (on ?x ?y)
	       (ontable ?x)
	       (clear ?x)
	       (handempty ?a)
	       (holding ?a ?x)
               (agent ?a)
	       )

  (:action pick-up
	     :parameters (?a ?x)
	     :precondition (and (agent ?a) (clear ?x) (ontable ?x) (handempty ?a))
	     :effect
	     (and (not (ontable ?x))
		   (not (clear ?x))
		   (not (handempty ?a))
		   (holding ?a ?x)))

  (:action put-down
	     :parameters (?a ?x)
	     :precondition (and (agent ?a) (holding ?a ?x))
	     :effect
	     (and (not (holding ?a ?x))
		   (clear ?x)
		   (handempty ?a)
		   (ontable ?x)))
  (:action stack
	     :parameters (?a ?x ?y)
	     :precondition (and (agent ?a) (holding ?a ?x) (clear ?y))
	     :effect
	     (and (not (holding ?a ?x))
		   (not (clear ?y))
		   (clear ?x)
		   (handempty ?a)
		   (on ?x ?y)))
  (:action unstack
	     :parameters (?a ?x ?y)
	     :precondition (and (agent ?a) (on ?x ?y) (clear ?x) (handempty ?a))
	     :effect
	     (and (holding ?a ?x)
		   (clear ?y)
		   (not (clear ?x))
		   (not (handempty ?a))
		   (not (on ?x ?y)))))
