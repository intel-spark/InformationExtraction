package com.intel.ie

import java.io.File

import com.intel.ie.analytics.{IOUtils, IntelKBPModel}
import com.intel.ie.relation.RelationExtractor
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
                         text: String)

object SparkBatchDriver {

  def main(args: Array[String]) {
    Logger.getLogger("org").setLevel(Level.WARN)
    println("loading models...")
    val sc = SparkContext.getOrCreate(
      new SparkConf()
        .setMaster("local")
        .setAppName(this.getClass.getSimpleName)
    )
    RelationExtractor.init()

    println("Initilization finished:")

    Iterator.continually(IOUtils.readLine("dataset path>")).foreach { line =>
      if (line.nonEmpty) Try {
        if (new File(line).exists()) {
          val data = getDataset(sc, line)
          if (line.endsWith(".csv")) {
            processCSVFiles(data)
          } else {
            processTextFiles(data).show(100, false)
          }
        } else {
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

  def getFullName(name: String): String = {
    fullNames.foreach(fullName => if (isFullName(name, fullName)) {
      if (name != fullName) println(name + " is replaced with " + fullName);
      return fullName
    })
    println(name);
    return name
  }

  private def processRDD(data: RDD[String]): DataFrame = {
    fullNames.clear()
    val relations = data.flatMap { s =>
      getWorkRelation(s)
    }.map(rl => RelationLine(getFullName(rl.name), rl.relation, rl.entity, rl.text))

    val sqlContext = SQLContext.getOrCreate(data.sparkContext)
    sqlContext.createDataFrame(relations)
  }

  def updateFullNames(name: String): Unit = {
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
    if (!isfull) fullNames += name
  }

  private def getWorkRelation(line: String): Seq[RelationLine] = {
    val raw = IntelKBPModel.extract(line)
    raw.asScala.toSeq.map { case (r, sen) =>
      if (r.relationGloss() == "org:top_members/employees") {
        RelationLine(r.objectGloss(), "top member of", r.subjectGloss(), sen)
      } else {
        updateFullNames(r.subjectGloss())
        RelationLine(r.subjectGloss(), r.relationGloss().split(":")(1), r.objectGloss(), sen)
      }
    }
  }

  var fullNames = new ListBuffer[String];

  private def isFullName(name: String, fullName: String): Boolean = {
    if (fullName.equals(name)) return true
    else if (fullName.contains(name)) {
      return true
    }
    else {
      name.split("\\s+|\\.").foreach(item => if (!fullName.contains(item)) {
        return false
      })
      return true
    }
  }

  private def getDataset(sc: SparkContext, path: String): RDD[String] = {

    val rdd = sc.textFile(path)
    rdd.unpersist(true)
    rdd.count()
    rdd
  }

}

