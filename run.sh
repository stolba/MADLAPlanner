#!/bin/bash
./parallel_experiments.sh -j solver.jar -c instances/$1/data.in -o instances/$1/data.out -v -s $2/:  
./addhead.sh instances/$1/data.out
