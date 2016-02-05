
require(plyr)
#library(RColorBrewer)
options(width = 160, height = 160)



data <- read.csv("avg-dth-per-problem.csv")


data$domainShort <- gsub("benchmarks/","",data$domain)
data$domainShort <- gsub("/domain.pddl","",data$domainShort)
data$problemShort <- gsub("benchmarks/","",data$problem)

data <- subset(data, data$domainShort != "sokoban")
data <- subset(data, data$domainShort != "transport")
data <- subset(data, data$domainShort != "mablocks")
data <- subset(data, data$domainShort != "rovers-large")
data <- subset(data, data$domainShort != "satellites-hc")

#data <- subset(data, data$domainShort != "satellites")
#data <- subset(data, data$domainShort != "rovers")
#data <- subset(data, data$domainShort != "logistics00")
#data <- subset(data, data$domainShort != "zenotravel")
#data <- subset(data, data$domainShort != "openstacks")

dataProj <- subset(data, data$heuristic == "FF" & data$recursionLevel == 0)
dataDist <- subset(data, data$heuristic == "PPsaFF-sync")

dataMerge <- merge(dataDist,dataProj,by="problemShort")

dataMerge$ratioExpanded <- dataMerge$expandedStatesM.y / dataMerge$expandedStatesM.x

statesPerDomain <- ddply(dataMerge, "domain.x", summarise,meanExpanded = mean(ratioExpanded))

out = format(statesPerDomain, scientific = FALSE, digits = 2, nsmall = 1)
write.table(out, "expanded_states_per_domain_dist_vs_proj.csv", quote = FALSE, sep = ",", row.names = FALSE)



columnx <- "expandedStatesM.y"
columny <- "expandedStatesM.x"



colors <- palette(c( "red2", "green2",  "blue2","red4", "lightsalmon4", "lightblue", "black", "gray50", "lightsalmon4", "darkgoldenrod1", "lightblue", "lightsalmon4", "lightsalmon4",  "blue3","red3", "gray75"))

dataMerge$plotchar <- as.numeric(dataMerge$domain.x)
dataMerge$color <- colors[as.numeric(dataMerge$domain.x)]

pdf("expandedStatesProjDistRatio.pdf")

getOption("scipen")
opt <- options("scipen" = 10)
getOption("scipen")
plot(dataMerge[,columnx],dataMerge[,columny],xlim=c(1,10000000), ylim=c(1,10000000),xlab="projected FF",ylab="distributed FF",log="xy",col=dataMerge$color,pch=dataMerge$plotchar) #plotchar[data_h1_h2$domain.x]
abline(0,1)
legend(1,10000000,unique(dataMerge$domainShort.x),cex=0.8,col=unique(dataMerge$color),pch=unique(dataMerge$plotchar))

dev.off()

dataDom <- subset(dataMerge,dataMerge$domainShort.x == "blocksworld")
h <- as.matrix(dataDom$expandedStatesM.x)
hd <- as.matrix(dataDom$expandedStatesM.y)
reg <- lm(hd ~ h) 
abline(1,summary(reg)$coefficients[2,1],col="red2",lty="dashed")

dataDom <- subset(dataMerge,dataMerge$domainShort.x == "woodworking08")
h <- as.matrix(dataDom$expandedStatesM.x)
hd <- as.matrix(dataDom$expandedStatesM.y)
reg <- lm(hd ~ h) 
abline(summary(reg)$coefficients[1,1],summary(reg)$coefficients[2,1],col="lightsalmon4",lty="twodash")

#dev.off()





