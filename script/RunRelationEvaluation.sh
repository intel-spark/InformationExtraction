#!/bin/bash

TargetJar=target/ie-project-1.0-SNAPSHOT-jar-with-dependencies.jar
MainClass=org.apache.spark.sql.RelationEvaluation
dataPath=data/evaluation/RelationEvaluation/web
labelPath=data/evaluation/RelationEvaluation/extraction

spark-submit \
  --master yarn \
  --driver-memory 20g \
  --executor-memory 20g \
  --num-executors 4 \
  --class $MainClass \
  --jars lib/stanford-english-corenlp-models-current.jar,intel-resources.jar \
  --files config.properties \
  $TargetJar \
  --partitionSize 8 \
  --withDetail false \
  $dataPath \
  $labelPath
  