
require(plyr)
options(width = 160)

BATCHES <- 10

data <- read.csv("out.csv")
data <- subset(data, data$planValid == "true")

multisummarise <- function (dt, heurs) {
    best = c()
    for(heur in heurs) {
        currRes <- summarise(subset(dt, heuristic == heur & length(agents) >= BATCHES / 2),
            totalTime      = mean(totalTime),
            expandedStates = mean(expandedStates),
            totalBytes     = mean(totalBytes),
            planLength     = mean(planLength))
        best <- rbind(best, currRes)
    }
    best = apply(best, 2, function(x) min(x, na.rm = TRUE))

    res <- summarise(dt, agents = mean(dt$agents))
    for(heur in heurs) {
        currRes <- summarise(subset(dt, heuristic == heur & length(agents) >= BATCHES / 2),
            round(mean(totalTime) / 100)/10,
            round(sqrt(var(totalTime)) / 100)/10,
            round(best['totalTime'] / mean(totalTime) * 100)/100,

            round(mean(expandedStates) / 100)/10,
            round(sqrt(var(expandedStates)) / 100)/10,
            round(best['expandedStates'] / mean(expandedStates) * 100)/100,

            round(mean(totalBytes) / 100000)/10,
            round(sqrt(var(totalBytes)) / 100000)/10,
            round(best['totalBytes'] / mean(totalBytes) * 100)/100,

            round(mean(planLength)),
            round(sqrt(var(planLength))),
            round(best['planLength'] / mean(planLength) * 100)/100)

	if(is.na(currRes)){
	    return(data.frame())
	}

        colnames(currRes) <- c(
            paste0(heur, "/totalTimeM"),
            paste0(heur, "/totalTimeV"),
            paste0(heur, "/totalTimeB"),

            paste0(heur, "/expandedStatesM"),
            paste0(heur, "/expandedStatesV"),
            paste0(heur, "/expandedStatesB"),

            paste0(heur, "/totalBytesM"),
            paste0(heur, "/totalBytesV"),
            paste0(heur, "/totalBytesB"),

            paste0(heur, "/planLengthM"),
            paste0(heur, "/planLengthV"),
            paste0(heur, "/planLengthB"))

        res <- cbind(res, currRes)
    }
    return(res)
}




# full table (depth-to-heuristics) -------------------------------------------------------

datac <- data
datac$heuristic <- paste0(datac$heuristic, "-h", datac$recursionLevel)

#heurs = unique(data$heuristic)
heurs = c("saFF-h0","saFF-sync-h-1","saFF-gl-h-1","saFF-glcl-h-1","saFF-sync-gRPrt50-h-1")

table1c <- ddply(datac, .(problem), multisummarise, heurs)
cnc <- colnames(table1c)
table1cbest <- colSums(table1c[,cnc[grep("B$",cnc)]], na.rm = TRUE)

table1c <- table1c[,cnc[grep("[^B]$",cnc)]]
table1c[is.na(table1c)] <- "--"
out = format(table1c, scientific = FALSE, digits = 1, nsmall = 1)
write.table(out, "per-problem-dth.csv", quote = FALSE, sep = "\t", row.names = FALSE, na = "--")

table1cbest = data.frame(val = table1cbest)
table1cbest = cbind(table1cbest, name = rownames(table1cbest))
table1cbest = within(table1cbest, {
    heur = gsub("([^/]*)/[^/]*", "\\1", name)
    metrics = gsub("[^/]*/([^/]*)", "\\1", name)
})

out <- tapply(table1cbest$val,list(table1cbest$heur,table1cbest$metrics), identity)
out <- format(out, scientific = FALSE, digits = 2, nsmall = 1)
write.table(out, "per-problem-dth-best.csv", quote = FALSE, sep = "\t")

# coverage IPC

table2ipc <- ddply(subset(datac, !grepl("ma-", data$domain)), .(heuristic), summarise,
                cvg = length(agents) / BATCHES)

out = format(table2ipc, scientific = FALSE, digits = 2, nsmall = 1)
write.table(out, "coverage-dth-ipc.csv", quote = FALSE, sep = "\t", row.names = FALSE)

# coverage IPC

table2ipc <- ddply(subset(datac, !grepl("ma-", data$domain)), .(domain, heuristic), summarise,
                cvg = length(agents) / BATCHES)

out = format(table2ipc, scientific = FALSE, digits = 2, nsmall = 1)
write.table(out, "coverage-dth-ipc-per-domain.csv", quote = FALSE, sep = "\t", row.names = FALSE)









stop("You shall not pass!") # ------------------------------------------------------------











# r-dependency heuristics graph (time)

datahg <- read.csv("out.csv")
datahg <- subset(datahg, datahg$planValid == "true")

graph2 <- ddply(datahg, .(heuristic, recursionLevel), summarize, totalTimeM = mean(totalTime))

graph2add = subset(graph2, graph2$heuristic == "add" & graph2$recursionLevel != -1)[2:3]
valinf = subset(graph2, graph2$heuristic == "add" & graph2$recursionLevel == -1)$totalTimeM
graph2add$totalTimeM <- graph2add$totalTimeM / valinf

graph2max = subset(graph2, graph2$heuristic == "max" & graph2$recursionLevel != -1)[2:3]
valinf = subset(graph2, graph2$heuristic == "max" & graph2$recursionLevel == -1)$totalTimeM
graph2max$totalTimeM <- graph2max$totalTimeM / valinf

graph2ff = subset(graph2, graph2$heuristic == "rdFF" & graph2$recursionLevel != -1)[2:3]
valinf = subset(graph2, graph2$heuristic == "rdFF" & graph2$recursionLevel == -1)$totalTimeM
graph2ff$totalTimeM <- graph2ff$totalTimeM / valinf

graph2lff = subset(graph2, graph2$heuristic == "lazyFF" & graph2$recursionLevel != -1)[2:3]
valinf = subset(graph2, graph2$heuristic == "lazyFF" & graph2$recursionLevel == -1)$totalTimeM
graph2lff$totalTimeM <- graph2lff$totalTimeM / valinf

pdf("graph-hr-time.pdf", width=5, height=4, family="Helvetica")
par(mar=c(3.0,3.0,1.0,1.0))

plot(graph2add, col="blue", type="b", pch=1,
     ylim=range(0, 2), xlim=range(0, 9),
     axes=F, xlab=NA, ylab=NA)
lines(graph2max, col="red", type="b", pch=2)
lines(graph2ff, col="green", type="b", pch=3)
lines(graph2lff, col="magenta", type="b", pch=4)

abline(h=1, col = "gray60")

legend("topright", inset=.05,
       c("add","max","rdFF","lazyFF"),
       pch=seq(1,4), col=c("blue", "red", "green","magenta"))
axis(side = 1, tck = -.005, labels = NA)
axis(side = 2, tck = -.005, labels = NA)
axis(side = 1, lwd = 0, line = -.6)
axis(side = 2, lwd = 0, line = -.6, las = 1)
mtext(side = 1, "recursion depth, r [-]", line = 1.5)
mtext(side = 2, "normalized planning time [-]", line = 1.5)
box()

dev.off()

#------------------

heurs = c("rdFF", "max", "add", "lazyFF")

for(heur in heurs) {
    datag <- read.csv("out.csv")
    datag <- subset(datag, datag$planValid == "true")
    datag <- subset(datag, datag$heuristic == heur)


    graph1 <- ddply(datag, .(domain, recursionLevel), summarize, totalTimeM = mean(totalTime))

    print(graph1)

    prob = "benchmarks/rov/domain.pddl"
    graph1rov = subset(graph1, graph1$domain == prob & graph1$recursionLevel != -1)[2:3]
    valinf = subset(graph1, graph1$domain == prob & graph1$recursionLevel == -1)$totalTimeM
    graph1rov$totalTimeM <- graph1rov$totalTimeM / valinf

    prob = "benchmarks/sat/domain.pddl"
    graph1sat = subset(graph1, graph1$domain ==  prob & graph1$recursionLevel != -1)[2:3]
    valinf = subset(graph1, graph1$domain == prob & graph1$recursionLevel == -1)$totalTimeM
    graph1sat$totalTimeM <- graph1sat$totalTimeM / valinf

    prob = "benchmarks/dec/domain.pddl"
    graph1dec = subset(graph1, graph1$domain == prob  & graph1$recursionLevel != -1)[2:3]
    valinf = subset(graph1, graph1$domain == prob & graph1$recursionLevel == -1)$totalTimeM
    graph1dec$totalTimeM <- graph1dec$totalTimeM / valinf

    prob = "benchmarks/log/domain.pddl"
    graph1log = subset(graph1, graph1$domain ==  prob & graph1$recursionLevel != -1)[2:3]
    valinf = subset(graph1, graph1$domain == prob & graph1$recursionLevel == -1)$totalTimeM
    graph1log$totalTimeM <- graph1log$totalTimeM / valinf

    pdf(paste0("graph-01248oo-", heur, ".pdf"), width=7, height=4, family="Helvetica")
    par(mar=c(3.0,3.0,1.0,1.0))

    plot(graph1rov, col="blue", type="b", pch=1,
         ylim=range(0, 2.5), xlim=range(0, 9),
         axes=F, xlab=NA, ylab=NA)
    #lines(graph1sat, col="red", type="b", pch=2)
    lines(graph1dec, col="darkgreen", type="b", pch=2)
    lines(graph1log, col="red", type="b", pch=3)

    abline(h=1, col = "gray60")

    legend("topright", inset=.05,
           c("rov","cpf","log"),
           pch=seq(1,4), col=c("blue", "darkgreen", "red"))
    axis(side = 1, tck = -.005, labels = NA)
    axis(side = 2, tck = -.005, labels = NA)
    axis(side = 1, lwd = 0, line = -.6)
    axis(side = 2, lwd = 0, line = -.6, las = 1)
    mtext(side = 1, "recursion depth [-]", line = 1.5)
    mtext(side = 2, "normalized planning time [-]", line = 2)
    box()

    dev.off()
}







