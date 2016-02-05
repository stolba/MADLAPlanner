
require(plyr)
options(width = 160)

interest <- c("domain","problem","heuristic","recursionLevel","totalTime","planLength","expandedStates")

data <- read.csv("out.csv")


avgpp <- ddply(data, .(domain,problem,heuristic,recursionLevel), summarise,
	totalTimeM = mean(totalTime),
	planLengthM = mean(planLength),
	expandedStatesM = mean(expandedStates)
)

out = format(avgpp , scientific = FALSE, digits = 2, nsmall = 1)
write.table(out, "avg-dth-per-problem.csv", quote = FALSE, sep = ",", row.names = FALSE)










