#!/bin/bash
#
# Command line parameter is FULL path to models directory
#

#FAC_CP="res/*"
FAC_CP="res/factorie-1.0-SNAPSHOT-jar-with-dependencies.jar:res/factorie-nlp-resources-0.1-SNAPSHOT.jar"
MODEL_DIR="file://$1"
POS_MODEL="OntonotesForwardPosTagger.factorie"
PARSE_MODEL="OntonotesTransitionBasedParser.factorie"
MEMORY="2g"

java -classpath $FAC_CP -Xmx$MEMORY -ea -Djava.awt.headless=true -Dfile.encoding=UTF-8 -server cc.factorie.app.nlp.NLP --ontonotes-forward-pos=$MODEL_DIR/$POS_MODEL --transition-based-parser=$MODEL_DIR/$PARSE_MODEL --wordnet-lemma

