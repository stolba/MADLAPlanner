(define (problem BLOCKS-5-0)
(:domain BLOCKS)
(:objects B E A C D a1 a2 a3 a4)
(:INIT (agent a1) (agent a2) (agent a3) (agent a4) (HANDEMPTY a1) (HANDEMPTY a2) (HANDEMPTY a3) (HANDEMPTY a4) (CLEAR D) (CLEAR C) (ONTABLE D) (ONTABLE A) (ON C E) (ON E B) (ON B A))
(:goal (AND (ON A E) (ON E B) (ON B D) (ON D C)))
)
