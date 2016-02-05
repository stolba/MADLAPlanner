
require(plyr)
#library(RColorBrewer)
options(width = 160, height = 160)

# je to na konci


data <- read.csv("out.csv")

data <- subset(data, data$initHeuristic != "")
data <- subset(data, data$initHeuristicDist != "")

data <- subset(data, data$heuristic == "PPsaFF-glcl")



#data$domain <- gsub("benchmarks/","",data$domain)
data$domainShort <- gsub("benchmarks/","",data$domain)

#data <- subset(data, data$domainShort != "satellites")
#data <- subset(data, data$domainShort != "rovers")
#data <- subset(data, data$domainShort != "logistics00")
#data <- subset(data, data$domainShort != "zenotravel")
#data <- subset(data, data$domainShort != "openstacks")

data$initHeuristic <- gsub("[[:alnum:]-]+=","",data$initHeuristic,perl=TRUE)
data$initHeuristic <- gsub("}","",data$initHeuristic,perl=TRUE)
data$initHeuristic <- gsub("{","",data$initHeuristic,perl=TRUE)

data$initHeuristic <- strsplit(data$initHeuristic," ")
data$initHeuristic <- lapply(data$initHeuristic,as.numeric)

data$initHeuristicMean <- lapply(data$initHeuristic,mean)



data$initHeuristicDist <- gsub("[[:alnum:]-]+=","",data$initHeuristicDist,perl=TRUE)
data$initHeuristicDist <- gsub("}","",data$initHeuristicDist,perl=TRUE)
data$initHeuristicDist <- gsub("{","",data$initHeuristicDist,perl=TRUE)

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
ltypes <- c("dashed","dotted","dotdash","longdash","twodash")

data$plotchar <- as.numeric(data$domain)
data$color <- colors[as.numeric(data$domain)]



pdf("heurisitcPDRatio.pdf")

getOption("scipen")
opt <- options("scipen" = 20)
getOption("scipen")

plot(data[,columnx],data[,columny],xlim=c(1,200),cex=0.8,lwd=1, ylim=c(1,300),xlab="projected FF",ylab="distributed FF",col=data$color,pch=data$plotchar)#,col=colors[data$domain],pch=plotchar[data$domain]) #plotchar[data_h1_h2$domain.x]
abline(0,1)
legend(130,85,unique(data$domainShort),cex=0.8,col=unique(data$color),pch=unique(data$plotchar),lty=ltypes)

dataDom <- subset(data,data$domainShort == "blocksworld")
h <- matrix(do.call(rbind,matrix(dataDom$initHeuristic)))
hd <- matrix(do.call(rbind,matrix(dataDom$initHeuristicDist)))
reg <- lm(hd ~ h) 
#abline(reg,col=colors[as.numeric("blocksworld")],lty=ltypes[as.numeric("blocksworld")])
abline(reg,col=colors[as.numeric("blocksworld")],lty=ltypes[as.numeric("blocksworld")])


dataDom <- subset(data,data$domainShort == "depot")
h <- matrix(do.call(rbind,matrix(dataDom$initHeuristic)))
hd <- matrix(do.call(rbind,matrix(dataDom$initHeuristicDist)))
reg <- lm(hd ~ h) 
abline(reg,col="green2",lty="dotted")

dataDom <- subset(data,data$domainShort == "driverlog")
h <- matrix(do.call(rbind,matrix(dataDom$initHeuristic)))
hd <- matrix(do.call(rbind,matrix(dataDom$initHeuristicDist)))
reg <- lm(hd ~ h) 
abline(reg,col="blue2",lty="dotdash")

dataDom <- subset(data,data$domainShort == "elevators08")
h <- matrix(do.call(rbind,matrix(dataDom$initHeuristic)))
hd <- matrix(do.call(rbind,matrix(dataDom$initHeuristicDist)))
reg <- lm(hd ~ h) 
abline(reg,col="red4",lty="longdash")

dataDom <- subset(data,data$domainShort == "woodworking08")
h <- matrix(do.call(rbind,matrix(dataDom$initHeuristic)))
hd <- matrix(do.call(rbind,matrix(dataDom$initHeuristicDist)))
reg <- lm(hd ~ h) 
abline(reg,col="lightsalmon4",lty="twodash")



dev.off()




