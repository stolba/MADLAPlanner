(define (problem BLOCKS-14-1)
(:domain BLOCKS)
(:objects K A F L D B M E J N H I C G a1 a2 a3 a4)
(:INIT (agent a1) (agent a2) (agent a3) (agent a4) (HANDEMPTY a1) (HANDEMPTY a2) (HANDEMPTY a3) (HANDEMPTY a4) (CLEAR G) (CLEAR C) (CLEAR I) (CLEAR H) (CLEAR N) (ONTABLE J)
 (ONTABLE E) (ONTABLE M) (ONTABLE B) (ONTABLE N) (ON G J) (ON C E) (ON I D)
 (ON D L) (ON L M) (ON H F) (ON F A) (ON A K) (ON K B)  )
(:goal (AND (ON J D) (ON D B) (ON B H) (ON H M) (ON M K) (ON K F) (ON F G)
            (ON G A) (ON A I) (ON I E) (ON E L) (ON L N) (ON N C)))
)