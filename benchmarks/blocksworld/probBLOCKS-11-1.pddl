(define (problem BLOCKS-11-1)
(:domain BLOCKS)
(:objects B C E A H K I G D F J a1 a2 a3 a4)
(:INIT (agent a1) (agent a2) (agent a3) (agent a4) (HANDEMPTY a1) (HANDEMPTY a2) (HANDEMPTY a3) (HANDEMPTY a4) (CLEAR J) (CLEAR F) (CLEAR D) (CLEAR G) (ONTABLE I) (ONTABLE K)
 (ONTABLE H) (ONTABLE A) (ON J I) (ON F E) (ON E K) (ON D C) (ON C H) (ON G B)
 (ON B A) )
(:goal (AND (ON B D) (ON D J) (ON J K) (ON K H) (ON H A) (ON A C) (ON C F)
            (ON F G) (ON G I) (ON I E)))
)