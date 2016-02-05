
require(plyr)
options(width = 160)

interest <- c("domain","problem","agents","heuristic","recursionLevel","totalTime","planLength","expandedStates")

data140723 <- read.csv("out140723cl_zapatFF.csv")
data140723 <- subset(data140723, data140723$planValid == "true")
data140723 <- subset(data140723, data140723$heuristic == "saFF" | data140723$heuristic == "saFF-sync")
data140723 <- data140723[interest]

data140804 <- read.csv("out140804cl_zapatFF.csv")
data140804 <- subset(data140804, data140804$planValid == "true")
data140804 <- subset(data140804, data140804$heuristic == "saFF" | data140804$heuristic == "saFF-sync")
data140804 <- data140804[interest]

data140826 <- read.csv("out140826cl_zapatFF.csv")
data140826 <- subset(data140826, data140826$planValid == "true")
data140826 <- subset(data140826, data140826$heuristic == "saFF" | data140826$heuristic == "saFF-sync")
data140826 <- data140826[interest]

data <- rbind(data140723,data140804)
data <- rbind(data,data140826)


avgpp <- ddply(data, .(domain,problem,agents,heuristic,recursionLevel), summarise,
	totalTimeM = mean(totalTime),
	planLengthM = mean(planLength),
	expandedStatesM = mean(expandedStates)
)

out = format(avgpp , scientific = FALSE, digits = 2, nsmall = 1)
write.table(out, "avg-agents-per-problem.csv", quote = FALSE, sep = ",", row.names = FALSE)










