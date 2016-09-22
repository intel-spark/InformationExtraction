#!/bin/bash
# Change the SPARK_HOME according to your setting.

SPARK_HOME=/opt/spark-1.6.2-bin-hadoop2.6
TargetJar=InformationExtraction-1.0-SNAPSHOT-jar-with-dependencies.jar
MainClass=com.intel.ie.SparkInteractiveDriver

$SPARK_HOME/bin/spark-submit \
  --master yarn \
  --driver-memory 20g \
  --executor-memory 20g \
  --num-executors 4 \
  --class $MainClass \
  --jars stanford-english-corenlp-models-current.jar \
  --files config.properties \
  $TargetJar 8
  