# !/bin.bash
#
#
#

if [ $# -eq 0 ]; then
	echo "not enough args \n ./cluster.sh [city] [clusters]"
	exit 1 
fi

CITY=$1
K=$2
MAHOUT_DIR=/home/wirving/Documents/mahout/trunk/bin

WRK_DIR=/home/wirving/Documents/Research_Forensics/cluster_results/LDA_${CITY}_20
mkdir ${WRK_DIR}
cd ${WRK_DIR}

# create sequence vectors
echo "#################################################"
echo "######### Creating sequence directory  ##########"
echo "#################################################"
#${MAHOUT_DIR}/mahout seqdirectory -i /home/wirving/Documents/Research_Forensics/${CITY}/ -o ./${CITY}_seq
echo "#################################################"
echo "#################  Done  ########################" 
echo "#################################################"

# create sparse tf-idf vectors
echo "#################################################"
echo "#############  Creating vectors #################"
echo "#################################################"
#${MAHOUT_DIR}/mahout seq2sparse -i ${CITY}_seq -o  ${CITY}_vector -ow -chunk 100 -x 90 -seq -ml 50 -n 2 -ng 5 -nv 
echo "#################################################"
echo "#################  Done  ########################" 
echo "#################################################"

# run kmeans clustering on data vectors
echo "#################################################"
echo "#############  Running K Means  #################"
echo "#################################################"
#${MAHOUT_DIR}/mahout kmeans -i doc_output -o kmeans-res -dm org.apache.mahout.common.distance.CosineDistanceMeasure -x 10 -ow -cd 1 -k $K -c centroids -cl
echo "#################################################"
echo "#################  Done  ########################"
echo "#################################################"

# export results data to text file
echo "#################################################"
echo "#########  Exporting data to txt file  ##########"
echo "#################################################"
#${MAHOUT_DIR}/mahout clusterdump  -i ${CITY}_result/clusters-1-final/ -o raw_out.txt -p  ${CITY}_result/clusteredPoints/
#cat raw_out.txt | awk '{print $1, $2}' | sed '/Weight :/d' > filter_out.txt
echo "#################################################"
echo "#################  Done  ########################" 
echo "#################################################"

# evaulate cluster results
cd /home/wirving/Documents/Research_Forensics/totally-legal-trafficking/
mvn compile && mvn exec:java -Dexec.mainClass=co.pemma.data.DataRunner -Dexec.args="${WRK_DIR}/labeled_${K}.txt" 
