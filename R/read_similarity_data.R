library("ff")
library(Matrix)

read.similarity.data <- function (directory)
{
  base.path <- "/NTFS/cs689"
  full.path <- paste(base.path, directory, sep='/')
  setwd(full.path)
  
  # read in the labels file
  labs = (read.csv("rlabels.txt",sep=",", header=FALSE))
  labs.df <- data.frame(key=labs[,1], label= labs[,2])
  # 
  size = length(labs.df[,1])
  
  # read in the data file - convert from similarity to dissimilarity
  data <-read.csv(file="rdata.txt",header=FALSE,nrows=((size**2)/2))
  # bound to [0,1]
  data[which(data[,3] < 0),3] <- 0
  # fill in the semtrical values
  data = rbind(data, cbind(data[,2],data[,1],data[,3]))
  # change to 1-indexing
  sparse.mat = sparseMatrix(data[,1],data[,2],x=(data[,3]))
  return (list(labels=labs.df, sparse.mat=sparse.mat))
}

read.threshold.data()