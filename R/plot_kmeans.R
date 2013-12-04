
library(ggplot2)
library(gridExtra)

n <-length(canopy[,1])


# canopy
colnames(canopy) <- c("k1","k2","Precision","Recall","F.Score")
tmp.data <- cbind(canopy[,1], canopy$Precision,rep('Precision',n))
tmp.data <- rbind(tmp.data, cbind(canopy[,1], canopy$Recall,rep('Recall',n)))
tmp.data <- rbind(tmp.data, cbind(canopy[,1], canopy$F.Score,rep('F',n)))
canopy.filter <- data.frame(tmp.data)
colnames(canopy.filter) <- c("K", "Value", "Measure")

p1 <- ggplot(data.frame(canopy.filter), aes(x=as.numeric(paste(canopy.filter$K)), y = as.numeric(paste(canopy.filter$Value)), group = Measure, color = Measure)) + geom_point() + geom_line() + theme_gray(20) + scale_y_continuous(limits = c(0 , 1)) + labs(x = "K", y = "Value", title = "Evaluation of KMeans after Canopy Clusters")

# K means
colnames(K) <- c("k1","k2","Precision","Recall","F.Score")
tmp.data <- cbind(K[,1], K$Precision,rep('Precision',n))
tmp.data <- rbind(tmp.data, cbind(K[,1], K$Recall,rep('Recall',n)))
tmp.data <- rbind(tmp.data, cbind(K[,1], K$F.Score,rep('F',n)))
K.filter <- data.frame(tmp.data)
colnames(K.filter) <- c("K", "Value", "Measure")

p2 <- ggplot(data.frame(K.filter), aes(x=as.numeric(paste(K.filter$K)), y = as.numeric(paste(canopy.filter$Value)), group = Measure, color = Measure)) + geom_point() + geom_line() + theme_gray(20) + scale_y_continuous(limits = c(0 , 1)) + labs(x = "K", y = "Value", title = "Evaluation of KMeans without Canopy")

# comparison
compare = data.frame(rbind(cbind(K$k1,K$Precision,rep('no canopy',n)), cbind(K$k1,canopy$Precision,rep('canopy',n))))
colnames(compare) <- c("K", "Value", "Measure")

p3 <- ggplot(data.frame(compare), aes(x=as.numeric(paste(compare$K)), y = as.numeric(paste(compare$Value)), group = Measure, color = Measure)) + geom_point() + geom_line() + theme_gray(20) + labs(x = "K", y = "Value", title = "Compare Precision scores of KMeans \n with and without Canopy")

# combine plots into 1 object
plots <- arrangeGrob(p1,p2,p3)

# save to pdf
ggsave("/home/pv/Downloads/cs689-plots.pdf", plots, width = 8, height = 11)