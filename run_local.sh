#!/bin/bash
#Arguments: <benchmark_folder> <problem name>     <add|max|rdFF|...> <recursion_depth> <time_limit(min)>
#           $1                 $2           	  $3                $4 			$5

#JAVA="/usr/lib/jvm/java-1.7.0-openjdk-amd64/bin/java"
JAVA="java"



echo timeout -s SIGSEGV $(($4+1))m $JAVA -Xmx8G -jar madla-planner.jar cz.agents.madla.creator.ProtobufCreator "./benchmarks/$1/domain.pddl" "./benchmarks/$1/$2.pddl" "./benchmarks/$1/$2.addl" $3 $4 $5

timeout -s SIGSEGV $(($4+1))m $JAVA -Xmx8G -jar madla-planner.jar cz.agents.madla.creator.ProtobufCreator "./benchmarks/$1/domain.pddl" "./benchmarks/$1/$2.pddl" "./benchmarks/$1/$2.addl" $3 $4 $5

  
