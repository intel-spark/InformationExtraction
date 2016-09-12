#!/bin/bash

SPARK_HOME=/opt/spark-1.6.2-bin-hadoop2.6
TargetJar=ie-project-1.0-SNAPSHOT-jar-with-dependencies.jar
MainClass=org.apache.spark.sql.RelationEvaluation
dataPath=hdfs://172.168.2.181:9000/user/yuhao/JPMC/data/evaluation/web/AECOM
labelPath=hdfs://172.168.2.181:9000/user/yuhao/JPMC/data/evaluation/extraction/

$SPARK_HOME/bin/spark-submit \
  --master yarn \
  --driver-memory 20g \
  --executor-memory 20g \
  --num-executors 4 \
  --class $MainClass \
  --jars stanford-english-corenlp-models-current.jar \
  --files config.properties \
  $TargetJar \
  --partitionSize 8 \
  --withDetail false \
  $dataPath \
  $labelPath
  
  