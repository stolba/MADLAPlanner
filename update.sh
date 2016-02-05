#!/bin/bash
mvn clean
mvn package
cp target/madla-planner-1.0-SNAPSHOT-jar-with-dependencies.jar release/madla-planner.jar
cp -rf benchmarks/ release/
cp -rf ma-benchmarks/ release/
cp -rf misc/ release/
