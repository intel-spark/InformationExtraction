package com.intel.ie

import java.io.File

import com.intel.ie.devUtils.RegexNerTest
import com.intel.ie.analytics.{IOUtils, IntelKBPModel}
import com.intel.ie.evaluation.preparation.crawl.Crawler
import org.apache.log4j.{Level, Logger}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.util.Try

case class RelationLine(
                         name: String,
                         relation: String,
                         entity: String,
                         text: String
                       )

object SparkBatchDriver {

  private var partitionSize = 8

  def main(args: Array[String]) {
    Logger.getLogger("org").setLevel(Level.WARN)
    println("loading models...")
    val sc = SparkContext.getOrCreate(
      new SparkConf()
        .setAppName(this.getClass.getSimpleName)
    )
    //    RelationExtractor.init()
    this.partitionSize = args(0).toInt
    processSentence("")

    Iterator.continually(IOUtils.readLine("dataset path>")).foreach { line =>
      if (line.nonEmpty) Try {
        if (new File(line).exists() || line.startsWith("hdfs")) {
          println(s"processing $line")
          val data = getDataset(sc, line)
          if (line.endsWith(".csv")) {
            processCSVFiles(data)
          } else {
            processTextFiles(data).show(100, false)
          }
        } else if (line.startsWith("http") || line.startsWith("www")) {
            val webContent = Crawler.crawlContent(line)
            val data = sc.parallelize(webContent, this.partitionSize)
            processTextFiles(data).show(100, false)
        }
        else {
          processSentence(line)
        }
      }
    }
  }

  def processTextFiles(data: RDD[String]): DataFrame = {
    val relations = processRDD(data)
    relations
  }

  private def processCSVFiles(data: RDD[String]): Unit = {
    val text = data.filter(!_.startsWith("//")) map (s =>
      s.split('|')(3)
      )
    val relations = processRDD(text)
    relations.show(100, false)
  }


  private def processSentence(line: String): Unit = {
    println(RegexNerTest.extractNER(line).asScala.mkString(", "))
    IntelKBPModel.extract(line).asScala.foreach(t => println(t._1))
  }

  private def processRDD(data: RDD[String]): DataFrame = {
    fullNames.clear()
    fullNameCache.empty
    val relations = data.flatMap { s =>
      getWorkRelation(s)
    }

    val sqlContext = SQLContext.getOrCreate(data.sparkContext)
    sqlContext.createDataFrame(relations)
  }

  def getWorkRelation(line: String): Seq[RelationLine] = {
    val raw = IntelKBPModel.extract(line)
    raw.asScala.toSeq.map { case (r, sen) =>
      if (r.relationGloss() == "org:top_members/employees") {
        RelationLine(r.objectGloss(), "top member of", r.subjectGloss(), sen)
      } else {
        updateFullNames(r.subjectGloss())
        RelationLine(r.subjectGloss(), r.relationGloss().split(":")(1), r.objectGloss(), sen)
      }
    }
      .map(rl => RelationLine(getFullName(rl.name), rl.relation, rl.entity, rl.text))
  }

  private def getDataset(sc: SparkContext, path: String): RDD[String] = {
    val rdd = sc.textFile(path, this.partitionSize)
    rdd
  }


  private var fullNameCache = Map[String, String]()

  private def getFullName(name: String): String = {
    if (fullNameCache.contains(name)) return fullNameCache(name)
    fullNames.foreach(fullName => if (isFullName(name, fullName)) {
      // if (name != fullName) println(Console.BLUE + name + Console.BLACK + " is replaced with " + Console.RED + fullName);
      fullNameCache += (name -> fullName)
      return fullName
    })
    name
  }

  private def updateFullNames(name: String): Unit = {
    var isfull = false
    fullNames = fullNames.map(fullName => {
      if (isFullName(name, fullName)) {
        isfull = true
        fullName
      }
      else if (isFullName(fullName, name)) {
        isfull = true
        name
      } else {
        fullName
      }
    })
    if (name.split("\\s+").length < 6 && !isfull) fullNames += name
  }

  private var fullNames = new ListBuffer[String];

  private def isFullName(name: String, fullName: String): Boolean = {
    if (name.split("\\s+").length > 6 || fullName.split("\\s+").length > 6) return false
    if (fullName.equals(name)) true
    else if (fullName.contains(name)) {
      true
    }
    else {
      name.split("\\s+|\\.").foreach(item => if (!fullName.contains(item)) {
        return false
      })
      true
    }
  }

}


