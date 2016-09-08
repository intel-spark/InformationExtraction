#!/bin/bash

SPARK_HOME=/opt/spark-1.6.2-bin-hadoop2.6
TargetJar=target/ie-project-1.0-SNAPSHOT-jar-with-dependencies.jar
MainClass=com.intel.ie.SparkBatchDriver


$SPARK_HOME/bin/spark-submit \
  --master yarn \
  --driver-memory 20g \
  --executor-memory 20g \
  --num-executors 4 \
  --class $MainClass \
  --jars lib/stanford-english-corenlp-models-current.jar,intel-resources.jar \
  --files config.properties \
  $TargetJar 8