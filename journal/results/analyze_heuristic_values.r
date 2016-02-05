
require(plyr)
#library(RColorBrewer)
options(width = 160, height = 160)


h1 <- "lm-cut"
h2 <- "ma-lm-cut"


data <- read.csv("out.csv")

data <- subset(data, data$initHeuristic != "")
data <- subset(data, data$initHeuristicDist != "")



#data$domain <- gsub("benchmarks/","",data$domain)
data$domainShort <- gsub("benchmarks/","",data$domain)

data <- subset(data, data$domainShort != "satellites")
data <- subset(data, data$domainShort != "rovers")
data <- subset(data, data$domainShort != "logistics00")
data <- subset(data, data$domainShort != "zenotravel")
data <- subset(data, data$domainShort != "openstacks")

data$initHeuristic <- gsub("[[:alnum:]-]+=","",data$initHeuristic,perl=TRUE)

data$initHeuristic <- strsplit(data$initHeuristic," ")
data$initHeuristic <- lapply(data$initHeuristic,as.numeric)

data$initHeuristicMean <- lapply(data$initHeuristic,mean)



data$initHeuristicDist <- gsub("[[:alnum:]-]+=","",data$initHeuristicDist,perl=TRUE)

data$initHeuristicDist <- strsplit(data$initHeuristicDist," ")
data$initHeuristicDist <- lapply(data$initHeuristicDist,as.numeric)

data$initHeuristicDistMean <- lapply(data$initHeuristicDist,mean)

data$label <- paste(data$domain,data$problem,sep="-")

columnx <- "initHeuristicMean"
columny <- "initHeuristicDistMean"


data_significant <- subset(data,as.numeric(initHeuristicMean) > as.numeric(initHeuristicDistMean))

data$ratioPD <- as.numeric(as.numeric(data$initHeuristicMean) / as.numeric(data$initHeuristicDistMean))

heursPerDomain <- ddply(data, "domain", summarise,meanPD = mean(ratioPD))

out = format(heursPerDomain, scientific = FALSE, digits = 2, nsmall = 1)
write.table(out, "heuristics_per_domain_all.csv", quote = FALSE, sep = ",", row.names = FALSE)


colors <- palette(c( "red2", "green2",  "blue2","red4", "green4", "lightblue", "black", "gray50", "lightsalmon4", "darkgoldenrod1"))

data$plotchar <- as.numeric(data$domain)
data$color <- colors[as.numeric(data$domain)]

pdf("heurisitcPDRatio.pdf")

getOption("scipen")
opt <- options("scipen" = 20)
getOption("scipen")

plot(data[,columnx],data[,columny],xlim=c(1,200), ylim=c(1,300),xlab="projected FF",ylab="distributed FF",col=data$color,pch=data$plotchar)#,col=colors[data$domain],pch=plotchar[data$domain]) #plotchar[data_h1_h2$domain.x]
abline(0,1)
legend(130,85,unique(data$domainShort),cex=0.8,col=unique(data$color),pch=unique(data$plotchar))


dev.off()



dataOpt <- read.csv("out_optimal.csv")

dataOpt$problem <- paste(dataOpt$domain,dataOpt$problem,sep="/") 
data$problem <- gsub("benchmarks/","",data$problem)
data$problem <- gsub(".pddl","",data$problem)

dataOpt <- merge(data,dataOpt,by="problem")

dataOpt$ratioPO <- as.numeric(as.numeric(dataOpt$initHeuristicMean) / as.numeric(dataOpt$optimalPlanLength))
dataOpt$ratioDO <- as.numeric(as.numeric(dataOpt$initHeuristicDistMean) / as.numeric(dataOpt$optimalPlanLength))
dataOpt$ratioPlanO <- as.numeric(as.numeric(dataOpt$planLength) / as.numeric(dataOpt$optimalPlanLength))

data_significant2 <- subset(dataOpt,as.numeric(ratioPlanO) < 1)


ratiosPerDomain <- ddply(dataOpt, "domain.x", summarise,meanPO = mean(ratioPO),meanDO = mean(ratioDO),meanPlanO = mean(ratioPlanO))

out = format(ratiosPerDomain, scientific = FALSE, digits = 2, nsmall = 1)
write.table(out, "ratios_per_domain_opt.csv", quote = FALSE, sep = ",", row.names = FALSE)



dataOptFinished <- subset(dataOpt,dataOpt$planValid == "true")
dataOptFinished$ratioPlanO <- as.numeric(as.numeric(dataOptFinished$planLength) / as.numeric(dataOptFinished$optimalPlanLength))

ratiosPerDomainFinished <- ddply(dataOptFinished, "domain.x", summarise,meanPlanO = mean(ratioPlanO))

out = format(ratiosPerDomainFinished, scientific = FALSE, digits = 2, nsmall = 1)
write.table(out, "ratios_per_domain_opt_finished.csv", quote = FALSE, sep = ",", row.names = FALSE)







