library(ggplot2)

inputData <- paste(Sys.getenv("BUMI_FAUNUS_DIR"),"/output/number-of-committed/sideeffect",sep="")

data <- read.table(inputData,col.names=c("degrees"))

m<-ggplot(data,aes(x=degrees))
m+geom_histogram(binwidth=1)
ggsave("analysis/people/number-of-commits.jpg")


inputData <- paste(Sys.getenv("BUMI_FAUNUS_DIR"),"/output/number-of-authored/sideeffect",sep="")

data <- read.table(inputData,col.names=c("degrees"))

m<-ggplot(data,aes(x=degrees))
m+geom_histogram(binwidth=1)
ggsave("analysis/people/number-of-authored.jpg")
