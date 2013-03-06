library(ggplot2)

inputData <- paste(Sys.getenv("BUMI_CURR"),"/output/number-of-committed/sideeffect",sep="")

data <- read.table(inputData,col.names=c("degrees"))

m<-ggplot(data,aes(x=degrees))+labs(title="Histogram of number of commits committed per person")
m<-m+geom_histogram(binwidth=1)
m<-m+scale_y_sqrt()+scale_x_sqrt()
ggsave("analysis/people/number-of-commits.jpg")


inputData <- paste(Sys.getenv("BUMI_CURR"),"/output/number-of-authored/sideeffect",sep="")

data <- read.table(inputData,col.names=c("degrees"))

m<-ggplot(data,aes(x=degrees))+labs(title="Histogram of number of commits authored per person")
m<-m+geom_histogram(binwidth=1)
m<-m+scale_y_sqrt()+scale_x_sqrt()
ggsave("analysis/people/number-of-authored.jpg")
