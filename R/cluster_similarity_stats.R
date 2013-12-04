library(ggplot2)
library(foreach)
library(plyr)

# read in data
setwd('/home/pv/Documents/cs689-class-proj/R')
source('read_similarity_data.R')
data <- read.similarity.data('20_function')

# cant analyze the unlabled data 
labeled.labels <- data.frame(subset(data$labels, label != -1))
class.counts <- aggregate( as.integer(key)~as.integer(label), labeled.labels, length)
colnames(class.counts) <- c("label", "count")

# analyze clusters with atleast k posts
min.count = 5
class.counts.threshold <- data.frame(subset(class.counts, count > min.count))
labels <- subset(labeled.labels, )

# split labels into differnent classes
class.lists <-split (labels, labels$label)

intra.class.similarities <- foreach (class=class.lists, .combine='cbind') %dopar%
{
  pairs <- expand.grid(key1=class[,1], key2=class[,1])
  pairs <- data.frame(key1=pairs[,1],key2=pairs[,2])
  pairs <- pairs[pairs$key1 > pairs$key2,]
  
  class.similarity <- ddply(pairs[1:50,], c("key1","key2"), .fun=function(x){ if(x$key1 == x$key2){ 0 } else { data$sparse.mat[x$key1,x$key2] } } )  
}

inter.cluster.similarity <- mean(data$sparse.mat)