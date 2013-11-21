# !/bin.bash
#
#
#

if [ $# -eq 0 ]; then
	echo "not enough args \n ./cluster.sh [K] [MIN POSTS]"
	exit 1 
fi

K=$1
MIN=$2
MAHOUT_DIR=/home/pv/Documents/mahout/bin
WRK_DIR=/NTFS/cs689/${MIN}
INPUT_DIR=${WRK_DIR}/reviews/

cd ${WRK_DIR}

# create sequence vectors
echo "#################################################"
echo "######### Creating sequence directory  ##########"
echo "#################################################"
${MAHOUT_DIR}/mahout seqdirectory -i ${INPUT_DIR} -o ./seq
echo "#################################################"
echo "#################  Done  ########################" 
echo "#################################################"

# create sparse tf-idf vectors
echo "#################################################"
echo "#############  Creating vectors #################"
echo "#################################################"
${MAHOUT_DIR}/mahout seq2sparse -i seq -o  vector -chunk 100 -x 90 -seq -ml 50 -n 2 -ng 5 -nv 
echo "#################################################"
echo "#################  Done  ########################" 
echo "#################################################"

# run kmeans clustering on data vectors
echo "#################################################"
echo "#############  Running K Means  #################"
echo "#################################################"
${MAHOUT_DIR}/mahout kmeans -i vector/tfidf-vectors/part-r-00000 -o  result -dm org.apache.mahout.common.distance.CosineDistanceMeasure -x 10 -cd 1 -k $K -c centroids -cl
echo "#################################################"
echo "#################  Done  ########################"
echo "#################################################"

# export results data to text file
echo "#################################################"
echo "#########  Exporting data to txt file  ##########"
echo "#################################################"
${MAHOUT_DIR}/mahout clusterdump  -i result/clusters-1-final/ -o raw_out.txt -p  result/clusteredPoints/
cat raw_out.txt | awk '{print $1, $2}' | sed '/Weight :/d' > filter_out.txt
${MAHOUT_DIR}/mahout clusterdump -i centroids/part-randomSeed > centroids.txt
echo "#################################################"
echo "#################  Done  ########################" 
echo "#################################################"

