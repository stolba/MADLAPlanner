(define (problem BLOCKS-4-2)
(:domain BLOCKS)
(:objects B D C A a1 a2 a3 a4)
(:INIT (agent a1) (agent a2) (agent a3) (agent a4) (HANDEMPTY a1) (HANDEMPTY a2) (HANDEMPTY a3) (HANDEMPTY a4) (CLEAR A) (CLEAR C) (CLEAR D) (ONTABLE A) (ONTABLE B) (ONTABLE D)
 (ON C B))
(:goal (AND (ON A B) (ON B C) (ON C D)))
)
