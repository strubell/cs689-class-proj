# !/bin.bash
#
#
#

if [ $# -le 2 ]; then
	echo "not enough args \n ./cluster.sh [input dir] [T1] [T2] "
	exit 1 
fi

INPUT=$1
T1=$2
T2=$3
MAHOUT_DIR=/home/pv/Documents/mahout/bin
WRK_DIR=/tmp/cs689/

mkdir ${WRK_DIR}
cd ${WRK_DIR}

# create sequence vectors
#echo "#################################################"
#echo "######### Creating sequence directory  ##########"
#echo "#################################################"
#${MAHOUT_DIR}/mahout seqdirectory -i ${INPUT_DIR} -o ./seq
#echo "#################################################"
#echo "#################  Done  ########################" 
#echo "#################################################"

# create sparse tf-idf vectors
#echo "#################################################"
#echo "#############  Creating vectors #################"
#echo "#################################################"
#${MAHOUT_DIR}/mahout seq2sparse -i ${INPUT} -o  vector -chunk 100 -x 90 -seq -ml 50 -n 2 -ng 5 -nv 
#echo "#################################################"
#echo "#################  Done  ########################" 
#echo "#################################################"

# generate canopy clusters
${MAHOUT_DIR}/mahout canopy -i ${INPUT} -o canopy -dm org.apache.mahout.common.distance.CosineDistanceMeasure -t1 $T1 -t2 $T2 -cl -ow 


# run kmeans clustering on data vectors
echo "#################################################"
echo "#############  Running K Means  #################"
echo "#################################################"
${MAHOUT_DIR}/mahout kmeans -i ${INPUT} -o  result -dm org.apache.mahout.common.distance.CosineDistanceMeasure -x 1000 -cd 0.1 -ow -c canopy/clusters-0-final -cl
echo "#################################################"
echo "#################  Done  ########################"
echo "#################################################"

# export results data to text file
echo "#################################################"
echo "#########  Exporting data to txt file  ##########"
echo "#################################################"
${MAHOUT_DIR}/mahout clusterdump  -i result/clusters-1-final/ -o raw_out.txt -p  result/clusteredPoints/
cat raw_out.txt | awk '{print $1, $2}' | sed '/Weight :/d' > filter_out.txt
#${MAHOUT_DIR}/mahout clusterdump -i centroids/part-randomSeed > centroids.txt
echo "#################################################"
echo "#################  Done  ########################" 
echo "#################################################"

