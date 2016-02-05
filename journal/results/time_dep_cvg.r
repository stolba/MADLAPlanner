
require(plyr)
#library(RColorBrewer)
options(width = 160, height = 160)



data <- read.csv("avg-dth-per-problem.csv")

data <- subset(data, data$totalTimeM != -1)

data$domainShort <- gsub("benchmarks/","",data$domain)
data$domainShort <- gsub("/domain.pddl","",data$domainShort)
data$problemShort <- gsub("benchmarks/","",data$problem)

data <- subset(data, data$domainShort != "sokoban")
data <- subset(data, data$domainShort != "transport")
data <- subset(data, data$domainShort != "mablocks")
data <- subset(data, data$domainShort != "rovers-large")
data <- subset(data, data$domainShort != "satellites-hc")

data1s <- subset(data, data$totalTimeM <= 1000)
data05 <- subset(data, data$totalTimeM <= 0.5*60*1000)
data1 <- subset(data, data$totalTimeM <= 1*60*1000)
data5 <- subset(data, data$totalTimeM <= 5*60*1000)
data10 <- subset(data, data$totalTimeM <= 10*60*1000)
data15 <- subset(data, data$totalTimeM <= 15*60*1000)

cvg <- ddply(data, "heuristic", summarise,cvg = length(heuristic))

dataTD <- ddply(data1s, "heuristic", summarise,cvg1s = length(heuristic))
dataTD <- merge(dataTD, ddply(data05, "heuristic", summarise,cvg30s = length(heuristic)),by="heuristic")
dataTD <- merge(dataTD, ddply(data1, "heuristic", summarise,cvg1m = length(heuristic)),by="heuristic")
dataTD <- merge(dataTD, ddply(data5, "heuristic", summarise,cvg5m = length(heuristic)),by="heuristic")
dataTD <- merge(dataTD, ddply(data10, "heuristic", summarise,cvg10m = length(heuristic)),by="heuristic")
dataTD <- merge(dataTD, ddply(data15, "heuristic", summarise,cvg15m = length(heuristic)),by="heuristic")


out = format(statesPerDomain, scientific = FALSE, digits = 2, nsmall = 1)
write.table(out, "expanded_states_per_domain_dist_vs_proj.csv", quote = FALSE, sep = ",", row.names = FALSE)






