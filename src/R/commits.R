data <- read.table("output/number-of-files-changed.txt")

jpeg("analysis/commits/number-of-files-changed.jpg")

plot(data,ann=FALSE)

title(main="Number of files changed by each commit")
title(xlab="Number of files changed")
title(ylab="Number of commits")

dev.off()

jpeg("analysis/commits/number-of-files-changed-log.jpg")

plot(data,ann=FALSE,log="xy")

title(main="Number of files changed by each commit")
title(xlab="Number of files changed")
title(ylab="Number of commits")

dev.off()
