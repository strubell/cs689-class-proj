# !/bin.bash
#
#

#if [ $# -eq 0 ]; then
#	echo "not enough args \n ./similarity.sh [directory]"
#	exit 1
#fi
#

MAHOUT_DIR=/home/pv/Documents/mahout/bin
WRK_DIR=/NTFS/cs689/$1

#cd ${WRK_DIR}
mkdir ../results
cd ../results

# create matrix from sparse vectors
echo "#################################################"
echo "############ Creating matrices  #################"
echo "#################################################"
${MAHOUT_DIR}/mahout rowid  -i ../output/features -o matrix
echo "#################################################"
echo "#################  Done  ########################"
echo "#################################################"

# find row count
ROW_COUNT=`${MAHOUT_DIR}/mahout seqdumper -i ../output/features |grep Count: | awk '{print $2}'`

# find pairwise distance 
echo "#################################################"
echo "#######  Calculating pairwise similarity  #######"
echo "#################################################"
${MAHOUT_DIR}/mahout rowsimilarity -i matrix/matrix -o similarity -r ${ROW_COUNT} -s SIMILARITY_COSINE -m 10 -ess
echo "#################################################"
echo "#################  Done  ########################" 
echo "#################################################"

# export labels to text file
echo "#################################################"
echo "#######  Exporting labels to txt file  ##########"
echo "#################################################"
${MAHOUT_DIR}/mahout seqdumper -i matrix/docIndex > labels.txt
#`cat labels.txt |awk '{ if ($1=="Key:" ) { {split($4,file,"/")} {print $2 file[2]":" substr(file[3],1,length(file[3])-4)}}}' > rlabels.txt`
echo "#################################################"
echo "#################  Done  ########################" 
echo "#################################################"

# export results data to text file
echo "#################################################"
echo "#########  Exporting data to txt file  ##########"
echo "#################################################"
${MAHOUT_DIR}/mahout seqdumper -i temp/pairwiseSimilarity/part-r-00000 > data.txt
#`cat data.txt | grep 'Key:' | tr -d "{" | tr -d "}" | awk '{ split($4, values, ","); row=substr($2,1,length($2)-1); for (i=1; i <= length(values); i++){split(values[i],value,":"); if(value[1] != row) print row+1","value[1]+1","1-value[2]}}' > rdata.txt`
echo "#################################################"
echo "#################  Done  ########################" 
echo "#################################################"

