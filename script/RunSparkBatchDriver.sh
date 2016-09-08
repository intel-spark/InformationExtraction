#!/bin/bash

SPARK_HOME=/opt/spark-1.6.2-bin-hadoop2.6
TargetJar=ie-project-1.0-SNAPSHOT-jar-with-dependencies.jar
MainClass=com.intel.ie.SparkBatchDriver

$SPARK_HOME/bin/spark-submit \
  --master yarn \
  --driver-memory 20g \
  --executor-memory 20g \
  --num-executors 4 \
  --class $MainClass \
  --jars stanford-english-corenlp-models-current.jar \
  --files config.properties \
  $TargetJar 8
  