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

WRK_DIR=/home/wirving/Documents/Research_Forensics/cluster_results/LDA_${CITY}_${K}
mkdir ${WRK_DIR}
cd ${WRK_DIR}

# create sequence vectors
echo "#################################################"
echo "######### Creating sequence directory  ##########"
echo "#################################################"
${MAHOUT_DIR}/mahout seqdirectory -i /home/wirving/Documents/Research_Forensics/${CITY}/ -o ./${CITY}_seq
echo "#################################################"
echo "#################  Done  ########################" 
echo "#################################################"

# create sparse tf vectors
echo "#################################################"
echo "#############  Creating vectors #################"
echo "#################################################"
${MAHOUT_DIR}/mahout seq2sparse -i ${CITY}_seq -o  ${CITY}_vector -wt tf -ow -chunk 100 -x 70 -seq -ml 50 -n 2 -ng 5 -nv 
echo "#################################################"
echo "#################  Done  ########################" 
echo "#################################################"

#Turn vectors into a matrix
echo "#################################################"
echo "#############  Converting to matrix #############"
echo "#################################################"
${MAHOUT_DIR}/mahout rowid -i ${CITY}_vector/tf-vectors/part-r-00000 -o ${CITY}_matrix
echo "#################################################"
echo "#############  Done  ######################"
echo "#################################################"

echo "#################################################"
echo "#############  Running LDA ######################"
echo "#################################################"
${MAHOUT_DIR}/mahout cvb0_local -i ${CITY}_matrix/matrix -d ${CITY}_vector/dictionary.file-* -top ${K} -do doc_output -to topic_output 
#${MAHOUT_DIR}/mahout cvb -i ${CITY}_matrix/matrix -o cvb_output --maxIter 20 -dict ${CITY}_vector/dictionary.file-* -k ${K} -dt doc_topic_dist
echo "#################################################"
echo "#################  Done  ########################"
echo "#################################################"

# export results data to text file
echo "#################################################"
echo "#########  Exporting data to txt file  ##########"
echo "#################################################"
${MAHOUT_DIR}/mahout vectordump -i topic_output -o vectordump -vs 20 -p true -d ${CITY}_vector/dictionary.file-* -dt sequencefile -sort topic_output
echo "#################################################"
echo "#################  Done  ########################" 
echo "#################################################"

#Get distribution of each document  over each topic
${MAHOUT_DIR}/mahout seqdumper -i doc_output -o seq
echo "#################################################"
echo "#########  Running kmeans  ######################"
echo "#################################################"
#${MAHOUT_DIR}/mahout seq2sparse -i doc_topic_dist -o doc_output_vector -ow -wt tfidf -chunk 100 -seq -ml 50 -n 2 -ng 5 -nv 

#${MAHOUT_DIR}/mahout kmeans -i doc_topic_dist -o ${CITY}_result -dm org.apache.mahout.common.distance.CosineDistanceMeasure -x 10 -ow -cd 1 -k $K -c centroids -cl
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
#cd /home/wirving/Documents/Research_Forensics/totally-legal-trafficking/
#mvn compile && mvn exec:java -Dexec.mainClass=co.pemma.data.DataRunner -Dexec.args="${WRK_DIR}/filter_out.txt"


