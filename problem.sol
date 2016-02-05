<?xml version = "1.0" encoding="UTF-8" standalone="yes"?>
<CPLEXSolution version="1.2">
 <header
   problemName="problem.clp"
   objectiveValue="14"
   solutionTypeValue="1"
   solutionTypeString="basic"
   solutionStatusValue="1"
   solutionStatusString="optimal"
   solutionMethodString="dual"
   primalFeasible="1"
   dualFeasible="1"
   simplexIterations="6"
   writeLevel="1"/>
 <quality
   epRHS="1e-06"
   epOpt="1e-06"
   maxPrimalInfeas="0"
   maxDualInfeas="0"
   maxPrimalResidual="0"
   maxDualResidual="0"
   maxX="14"
   maxPi="1"
   maxSlack="21"
   maxRedCost="1"
   kappa="43.75"/>
 <linearConstraints>
  <constraint name="c1" index="0" status="LL" slack="0" dual="1"/>
  <constraint name="c2" index="1" status="BS" slack="1" dual="-0"/>
  <constraint name="c3" index="2" status="BS" slack="1" dual="-0"/>
  <constraint name="c4" index="3" status="BS" slack="11" dual="-0"/>
  <constraint name="c5" index="4" status="LL" slack="0" dual="-0"/>
  <constraint name="c6" index="5" status="BS" slack="2" dual="-0"/>
  <constraint name="c7" index="6" status="BS" slack="0" dual="-0"/>
  <constraint name="c8" index="7" status="LL" slack="0" dual="1"/>
  <constraint name="c9" index="8" status="LL" slack="0" dual="0"/>
  <constraint name="c10" index="9" status="BS" slack="2" dual="-0"/>
  <constraint name="c11" index="10" status="LL" slack="0" dual="-0"/>
  <constraint name="c12" index="11" status="LL" slack="0" dual="1"/>
  <constraint name="c13" index="12" status="LL" slack="0" dual="1"/>
  <constraint name="c14" index="13" status="BS" slack="21" dual="-0"/>
  <constraint name="c15" index="14" status="LL" slack="0" dual="1"/>
  <constraint name="c16" index="15" status="LL" slack="0" dual="-0"/>
 </linearConstraints>
 <variables>
  <variable name="P_truck" index="0" status="LL" value="0" reducedCost="-0"/>
  <variable name="P_crane" index="1" status="BS" value="14" reducedCost="-0"/>
  <variable name="am1559123304_truck" index="2" status="BS" value="-1" reducedCost="-0"/>
  <variable name="am880110951_truck" index="3" status="BS" value="0" reducedCost="-0"/>
  <variable name="am1559122344_truck" index="4" status="BS" value="1" reducedCost="-0"/>
  <variable name="am2130558656_truck" index="5" status="BS" value="11" reducedCost="-0"/>
  <variable name="am2130558657_crane" index="6" status="BS" value="11" reducedCost="-0"/>
  <variable name="am1559123304_crane" index="7" status="BS" value="2" reducedCost="-0"/>
  <variable name="am880110951_crane" index="8" status="BS" value="1" reducedCost="-0"/>
  <variable name="am1559122344_crane" index="9" status="BS" value="0" reducedCost="-0"/>
  <variable name="am2130558656_crane" index="10" status="LL" value="-10" reducedCost="-0"/>
  <variable name="am2130558657_truck" index="11" status="LL" value="-10" reducedCost="-1"/>
  <variable name="am880110952_crane" index="12" status="LL" value="-10" reducedCost="-0"/>
  <variable name="am880110952_truck" index="13" status="LL" value="-10" reducedCost="-0"/>
 </variables>
</CPLEXSolution>
