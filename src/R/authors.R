labelPlotfn <- function(label){
  data <- read.table(paste("output/authors-",label,".txt",sep=""))

  jpeg(paste("analysis/authors-",label,".jpg",sep=""))

  plot(data,ann=FALSE)

  title(main=paste("People mentioned in a commit message by a \"",label,"\" line",sep=""))
  title(xlab="Degree")
  title(ylab="Number of People")

  dev.off()


  
  jpeg(paste("analysis/authors-",label,"-log.jpg",sep=""))
  
  plot(data,log="xy",ann=FALSE)
  
  title(main=paste("People mentioned in a commit message by a \"",label,"\" line",sep=""))
  title(xlab="Degree")
  title(ylab="Number of People")

  dev.off()
  
}

Map(labelPlotfn,c("authored","committed","Cc","Signed-off-by","Acked-by","Reported-by"))

