(define (domain truck-crane)
  (:requirements :strips :typing)
  (:types
      location - object)

  (:predicates
    (truck-started)
    (truck-at ?location - location)
    (box-at ?location - location)
    (box-at-truck))

  (:action load-crane
    :parameters
        (?loc - location)
    :precondition
        (and (truck-at ?loc)
             (box-at ?loc))
    :effect
        (and (not (box-at ?loc))
             (box-at-truck)))

  (:action unload-crane
    :parameters
        (?loc - location)
    :precondition
        (and (truck-at ?loc)
             (box-at-truck))
    :effect
        (and (not (box-at-truck))
             (box-at ?loc)))

  (:action start-truck
    :parameters
        ()
    :precondition
        (and)
    :effect
        (truck-started))

  (:action move-truck
    :parameters
        (?loc-from - location
         ?loc-to - location)
    :precondition
        (and (truck-started) (truck-at ?loc-from))
    :effect
        (and (not (truck-at ?loc-from))
             (truck-at ?loc-to))))
