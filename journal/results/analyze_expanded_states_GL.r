
require(plyr)
#library(RColorBrewer)
options(width = 160, height = 160)



data <- read.csv("avg-dth-per-problem-details.csv")

#data <- subset(data, data$planValid == "true")
data <- subset(data, data$heuristic == "PPsaFF-glcl")

data$domainShort <- gsub("benchmarks/","",data$domain)
data$problemShort <- gsub("benchmarks/","",data$problem)

data <- subset(data, data$domainShort != "mablocks")
data <- subset(data, data$domainShort != "rovers-large")
data <- subset(data, data$domainShort != "satellites-hc")


columnx <- "expandedStatesLocalM"
columny <- "expandedStatesGlobalM"

data$ratioLE <- as.numeric(as.numeric(data$expandedStatesLocalM) / as.numeric(data$expandedStatesM))
data$ratioGE <- as.numeric(as.numeric(data$expandedStatesGlobalM) / as.numeric(data$expandedStatesM))
data$ratioGL <- as.numeric(as.numeric(data$expandedStatesGlobalM) / as.numeric(data$expandedStatesLocalM))
data$ratioLG <- as.numeric(as.numeric(data$expandedStatesLocalM) / as.numeric(data$expandedStatesGlobalM))

data$heurTimePerStateL <- as.numeric(as.numeric(data$localHeuristicTimeM) / as.numeric(data$expandedStatesLocalM))
data$heurTimePerStateG <- as.numeric(as.numeric(data$globalHeuristicTimeM) / as.numeric(data$expandedStatesGlobalM))
data$heurTimePerStateGO <- as.numeric((as.numeric(data$globalHeuristicTimeM)+as.numeric(data$otherHeuristicTimeM)) / as.numeric(data$expandedStatesGlobalM))
data$heurRatio <- data$heurTimePerStateGO / data$heurTimePerStateL


data <- subset(data, data$ratioLE != 0)
data <- subset(data, data$ratioGE != 0)

statesPerDomain <- ddply(data, "domain", summarise,meanLE = mean(ratioLE),meanGE = mean(ratioGE),meanGL = mean(ratioGL),meanLG = mean(ratioLG),meanHeurTimePerStateL = mean(heurTimePerStateL), meanHeurTimePerStateG = mean(heurTimePerStateG), meanHeurTimePerStateGO = mean(heurTimePerStateGO), meanHeurRatio = mean(heurRatio))

out = format(statesPerDomain, scientific = FALSE, digits = 2, nsmall = 1)
write.table(out, "expanded_states_per_domain.csv", quote = FALSE, sep = ",", row.names = FALSE)


colors <- palette(c( "red2", "green2",  "blue2","red4", "green4", "lightblue", "black", "gray50", "lightsalmon4", "darkgoldenrod1", "lightblue", "lightsalmon4", "green3",  "blue3","red3", "gray75"))

data$plotchar <- as.numeric(data$domain)
data$color <- colors[as.numeric(data$domain)]

pdf("expandedStatesLocalGlobalRatio.pdf")

getOption("scipen")
opt <- options("scipen" = 10)
getOption("scipen")
plot(data[,columnx],data[,columny],xlim=c(1,10000000), ylim=c(1,10000000),xlab="projected FF",ylab="distributed FF",log="xy",col=data$color,pch=data$plotchar) #plotchar[data_h1_h2$domain.x]
abline(0,1)
#text(exp(log(data_h1_h2[,columnx])+offsetx),exp(log(data_h1_h2[,columny])+offsety),labels=data_h1_h2$domain.x)
#text(exp(log(data_h1_h2[,columnx])+offsetx),exp(log(data_h1_h2[,columny])+offsety),labels=data_h1_h2$problem)
#text(exp(log(data_h1_h2[,columnx])+offsetx),exp(log(data_h1_h2[,columny])+offsety),labels=data_h1_h2$label)
legend(1,10000000,unique(data$domainShort),cex=0.8,col=unique(data$color),pch=unique(data$plotchar))


dev.off()



pdf("heuristicTimeLocalGlobalRatio.pdf")

#data <- subset(data,data$domainShort == "rovers")

getOption("scipen")
opt <- options("scipen" = 10)
getOption("scipen")
plot(data$heurTimePerStateL,data$heurTimePerStateG,xlim=c(0.001,20), ylim=c(0.001,250),log="x",xlab="projected FF",ylab="distributed FF",col=data$color,pch=data$plotchar) #plotchar[data_h1_h2$domain.x]
abline(0,1)
#text(data$heurTimePerStateL,exp(log(data$heurTimePerStateG)),labels=data$problemShort)
legend(0.001,250,unique(data$domainShort),cex=0.8,col=unique(data$color),pch=unique(data$plotchar))


dev.off()

pdf("heuristicTimeLocalGlobalOtherRatio.pdf")

#data <- subset(data,data$domainShort == "rovers")

getOption("scipen")
opt <- options("scipen" = 10)
getOption("scipen")
plot(data$heurTimePerStateL,data$heurTimePerStateGO,xlim=c(0.001,20), ylim=c(0.001,250),log="x",xlab="projected FF",ylab="distributed FF",col=data$color,pch=data$plotchar) #plotchar[data_h1_h2$domain.x]
abline(0,1)
#text(data$heurTimePerStateL,exp(log(data$heurTimePerStateG)),labels=data$problemShort)
legend(0.001,250,unique(data$domainShort),cex=0.8,col=unique(data$color),pch=unique(data$plotchar))


dev.off()


out = format(data, scientific = FALSE, digits = 2, nsmall = 1)
write.table(out, "out_avg_extended.csv", quote = FALSE, sep = ",", row.names = FALSE)


openstacks <- subset(data, domainShort == "openstacks")
