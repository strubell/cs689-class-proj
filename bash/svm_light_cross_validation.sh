#!/bin/bash
# Usage: svm_light_cross_validation full_data num_train_items num_test_items k_cycles results_file
RESULT_FILE=$5
 
echo "Running SVM-Light via cross validation on" $1 "by using" $2 "training items and" $3 "test items (Total number of cross-validation cycles:" $4 > $RESULT_FILE
 
MODEL_FILE="model."$RANDOM".txt"
TEMP_FILE="tempFile."$RANDOM".txt"
PRED_FILE="prediction."$RANDOM".txt"
DATA_FILE=$1
NUM_TRAIN=$2
NUM_TEST=$3
NUM_CYCLES=$4
 
TEMP_DATA_FILE=$DATA_FILE"."$RANDOM".temp"
TRAIN_FILE=$TEMP_DATA_FILE".train"
TEST_FILE=$TEMP_DATA_FILE".test"
 
TEMP_RESULT=$RESULT_FILE".temp"
SVM_PATTERN='s/Accuracy on test set: \([0-9]*.[0-9]*\)% ([0-9]* correct, [0-9]* incorrect, [0-9]* total)\.*/'
for k in `seq 1 $NUM_CYCLES`
do
 sort -R $DATA_FILE > $TEMP_DATA_FILE
 head -n $NUM_TRAIN $TEMP_DATA_FILE > $TRAIN_FILE
 tail -n $NUM_TEST $TEMP_DATA_FILE > $TEST_FILE
  
 echo "------------------------------------------"  >> $RESULT_FILE
 echo "Cross-validation cycle:" $k >> $RESULT_FILE
  
 # first run svm with default parameters
 echo ""  >> $RESULT_FILE
 echo "Polynomial SVM with default parameters" >> $RESULT_FILE
 for i in 1 2 3 4 5 6 7 8 9 10
 do
  echo "order:" $i >> $RESULT_FILE
  ./svm_multiclass_learn -t 1 -d $i $TRAIN_FILE $MODEL_FILE > $TEMP_FILE
  ./svm_multiclass_classify -v 1 $TEST_FILE $MODEL_FILE $PRED_FILE > $TEMP_RESULT
  cat $TEMP_RESULT >> $RESULT_FILE
  sed '/^Reading model/d' $TEMP_RESULT > $TEMP_RESULT"1"
  sed '/^Precision/d' $TEMP_RESULT"1" > $TEMP_RESULT
  sed "$SVM_PATTERN$k poly $i \1/g" $TEMP_RESULT >> "better"$RESULT_FILE
 done
 
 echo ""  >> $RESULT_FILE
 echo "RBF SVM with default parameters" >> $RESULT_FILE
 for g in 0.00001 0.0001 0.001 0.1 1 2 3 5 10 20 50 100 200 500 1000
 do
  echo "gamma:" $g >> $RESULT_FILE
  ./svm_learn -t 2 -g $g $TRAIN_FILE $MODEL_FILE > $TEMP_FILE
  ./svm_classify -v 1 $TEST_FILE $MODEL_FILE $PRED_FILE >> $TEMP_FILE
  cat $TEMP_RESULT >> $RESULT_FILE
  sed '/^Reading model/d' $TEMP_RESULT > $TEMP_RESULT"1"
  sed '/^Precision/d' $TEMP_RESULT"1" > $TEMP_RESULT
  sed "$SVM_PATTERN$k rbf $g \1/g" $TEMP_RESULT >> "better"$RESULT_FILE
 done
 
done
 
rm $MODEL_FILE $TEMP_FILE $PRED_FILE $TEMP_DATA_FILE $TEMP_RESULT $TEMP_RESULT"1"
echo "Done." >> $RESULT_FILE