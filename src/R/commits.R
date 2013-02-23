library(ggplot2)
data <- read.table("output/number-of-files-changed.txt",col.names=c("degrees"))


m<-ggplot(data,aes(x=degrees))
m+geom_histogram(binwidth=1)
ggsave("analysis/commits/number-of-files-changed.jpg")



## hist(data$V1,ann=FALSE,breaks=max(data$V1),log="xy")

## title(main="Number of files changed by each commit")
## title(xlab="Number of files changed")
## title(ylab="Number of commits")

## ggsave("analysis/commits/number-of-files-changed-log.jpg")


