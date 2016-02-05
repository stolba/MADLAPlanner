library(plyr)
env <- "empty"
dir <- paste("instances/",env, sep="")
results <- read.csv(file=paste(dir, "/data.out.head", sep=""), head=TRUE, sep=";")
resultssorted <- results[order(results$instance),]

 
## successrate 

successrate <- ddply(results[results$radius==100,], .(nagents, alg), summarise, 
                    successrate = sum(is.finite(cost)) / length(cost) 
                    )

plot(successrate[successrate$alg=="PP","successrate"])
       

plot(successrate[successrate$alg=="PP","nagents"], 
     successrate[successrate$alg=="PP","successrate"]*100, 
     type="o", ylim=c(0,100), 
     ylab="% solved",
     xlab="number of agents")

points(successrate[successrate$alg=="IIHP","nagents"], 
       successrate[successrate$alg=="IIHP","successrate"]*100, 
       type="o", pch=22, col="red", lty=2)

points(successrate[successrate$alg=="OD","nagents"], 
       successrate[successrate$alg=="OD","succeeded"]*100, 
       type="o",  pch=23, col="forestgreen", lty=3)


title(main="% solved/no of agent")

## quality 

instances <- results[,c("nagents","seed",)]

successrate <- ddply(results, .(nagents, seed), summarise, 
                     pp = sum(is.finite(cost)) / length(cost)                    
)
  
costspp <- results[results$alg=="PP", c("nagents","seed","cost")]
costspp <- costspp[order(costspp$nagents, costspp$seed),]

costsiihp <- results[results$alg=="IIHP", c("nagents","seed","cost")]
costsiihp <- costsiihp[order(costsiihp$nagents, costsiihp$seed),]



costs.sum.pp <- ddply(costspp, .(nagents), summarise, 
                      mean=mean())

plot(costspp$cost)
points( costsiihp$cost, col="red", type="p", pch=4)


