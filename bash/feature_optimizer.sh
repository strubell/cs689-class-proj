#!/bin/bash
#
#

ROOT_DIR=/home/pv/Documents/cs689-class-proj
BASH_DIR=${ROOT_DIR}/bash
RESULT_FILE=feature_results.txt

FLAGS=('ws' 'wc' 'l' 'd'  'wl'  'p'  'sc'  'pos'  'dp'  'fw'  'y')

cd ${ROOT_DIR}
rm ${RESULT_FILE}
touch ${RESULT_FILE}

mvn compile

if [ "$1" == "all" ]; then
	FLAGS=`echo ${FLAGS[@]}`
fi

for FLAG in "${FLAGS[@]}"
do
	# compile data
	mvn exec:java -Dexec.mainClass=co.pemma.MLClassProj.GetSyntacticFeatures -Dexec.args="${FLAG}"
	rm -rf results

	cd ${BASH_DIR}
	# get pairwise similarity with mahout
	./function_similarity.sh
	cd ${ROOT_DIR}
	echo "${FLAG}" >> ${RESULT_FILE}
	# analyze mahout similarity results
	mvn compile && mvn exec:java -Dexec.mainClass=co.pemma.MLClassProj.Analysis >> ${RESULT_FILE}
done

