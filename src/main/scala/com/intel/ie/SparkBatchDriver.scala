package com.intel.ie

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SaveMode
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Process text files on local file system or HDFS
  * 
  * arg(0): File/Folder path to be processed
  * arg(1): partition number
  */
object SparkBatchDriver {

  def main(args: Array[String]) {
    Logger.getLogger("org").setLevel(Level.WARN)
    Logger.getLogger("com").setLevel(Level.WARN)
    
    val path = args(0)
    val partitionSize = args(1).toInt

    println("loading models...")
    val sc = SparkContext.getOrCreate(
      new SparkConf()
        .setAppName(this.getClass.getSimpleName)
    )
    sc.hadoopConfiguration.set("mapreduce.input.fileinputformat.input.dir.recursive", "true")
    val rdd = sc.textFile(path).coalesce(partitionSize, false)
    val relations = SparkInteractiveDriver.processRDD(rdd).cache()
    
    relations.write.format("json").mode(SaveMode.Overwrite).save("outputTable")
    relations.show(100, false)
  }

}
