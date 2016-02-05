\\LP for plan-based cost-partitioning

Maximize
 obj: P_truck + P_crane
Subject To
P_truck - am1559123304_truck - am880110951_truck <= 1.0
P_truck - am1559123304_truck - am880110951_truck <= 2.0
P_truck - am1559123304_truck - am880110951_truck - am1559122344_truck <= 1.0
P_truck - am1559123304_truck - am880110951_truck - am2130558656_truck <= 1.0
P_truck - am1559123304_truck - am880110951_truck - am880110951_truck <= 1.0
P_truck - am1559123304_truck - am880110951_truck - am1559122344_truck <= 2.0
P_truck - am1559123304_truck - am880110951_truck - am1559122344_truck - am1559123304_truck <= 1.0
P_crane - am2130558657_crane - am1559123304_crane - am880110951_crane <= 0.0
P_crane - am2130558657_crane - am1559123304_crane - am880110951_crane - am1559122344_crane <= 0.0
P_crane - am2130558657_crane - am1559123304_crane - am880110951_crane - am1559122344_crane - am1559123304_crane <= 0.0
 + am2130558656_crane + am2130558656_truck <= 1.0
 + am2130558657_crane + am2130558657_truck <= 1.0
 + am1559123304_crane + am1559123304_truck <= 1.0
 + am880110952_crane + am880110952_truck <= 1.0
 + am880110951_crane + am880110951_truck <= 1.0
 + am1559122344_crane + am1559122344_truck <= 1.0
Bounds
am880110951_crane >= -10
am2130558656_truck >= -10
am1559123304_truck >= -10
am1559123304_crane >= -10
am1559122344_crane >= -10
am2130558657_truck >= -10
am2130558656_crane >= -10
am2130558657_crane >= -10
am880110951_truck >= -10
am1559122344_truck >= -10
am880110952_truck >= -10
am880110952_crane >= -10
End