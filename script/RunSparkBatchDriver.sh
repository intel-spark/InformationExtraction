#!/bin/bash
# Change the SPARK_HOME according to your setting.
# set executor memory as necessary
#  --executor-memory 20g \
#  --num-executors 4 \

SPARK_HOME=/opt/spark-1.6.2-bin-hadoop2.6
TargetJar=InformationExtraction-1.0-SNAPSHOT-jar-with-dependencies.jar
MainClass=com.intel.ie.SparkBatchDriver
FilePath=hdfs://172.168.2.181:9000/user/yuhao/JPMC/data/evaluation/web/

$SPARK_HOME/bin/spark-submit \
  --master local[*] \
  --driver-memory 8g \
  --class $MainClass \
  --jars stanford-english-corenlp-models-current.jar \
  --files config.properties \
  $TargetJar $FilePath 8
  