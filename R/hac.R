library(fastcluster)
library(Matrix)
library(plyr)

start.time = proc.time()
# read data
setwd('/home/pv/Documents/totally-legal-trafficking/R')
source('read_similarity_data.R')
data <- read.similarity.data('all_no_images')

# get required distance object
distance.object <- as.dist(data$sparse.mat) 

# run hac
hac <- hclust(distance.object, method="average")

# merges and dissimilarity
merge.height <- cbind(hac$merge,hac$height)

# plot the dendogram
plot (hac, labels=data$labels[,2])

# get counts for clusters when k = 200
clusters.k <- cutree(hac, k=200) 
counts.k <- table(clusters.k)

# get counts for clusters when similarity threshold = .2
clusters.h <- cutree(hac, h=.3) 
counts.h <- table(clusters.h)


## analyze cluster results
index = 1:length(counts.h)
# ply each cluster
result = sapply(index, FUN=function(i)
{
    # get filenames from cluster
    file.names <- data$labels$file[clusters.h == i]
    # get index from each file
    rows <- ldply(file.names, .fun=function(x)
      {
      which(data$labels$file==x)
    })
    rows <- unlist(rows)
    # get average of intracluster dissimilarities
    r = outer(rows, t(rows), FUN=Vectorize( function(i,j) 
      if(i == j) 1 else data$sparse.mat[i,j]))
    avg.dissimilarity <- sum(r)/(length(r)**2 - length(r))
    ret <- rbind(length(rows), avg.dissimilarity)
})

# avg disimilarity of clusters with atleast 5 posts
mean(result[2,which(result[1,] > 5)])
# count of clusters with atleast 5 posts
length(result[2,which(result[1,] > 5))]

proc.time() - start.time


