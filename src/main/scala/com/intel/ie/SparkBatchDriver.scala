package com.intel.ie

import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by yuhao on 9/15/16.
  */
object SparkBatchDriver {

  def main(args: Array[String]) {
    Logger.getLogger("org").setLevel(Level.WARN)
    val path = args(0)
    val partitionSize = args(1).toInt

    println("loading models...")
    val sc = SparkContext.getOrCreate(
      new SparkConf()
        .setAppName(this.getClass.getSimpleName)
    )
    sc.hadoopConfiguration.set("mapreduce.input.fileinputformat.input.dir.recursive", "true")
    val rdd = sc.textFile(path, partitionSize)
    val relations = SparkInteractiveDriver.processRDD(rdd)

    relations.show(100, false)
    
  }

}
