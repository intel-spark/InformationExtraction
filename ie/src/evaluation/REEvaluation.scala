package evaluation

import java.io.File

import main.{RelationLine, SparkBatchDriver}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}
import relation.RelationExtractor
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._

import scala.util.Try

/**
  * Created by yuhao on 8/1/16.
  */
object REEvaluation {

  def main(args: Array[String]) {
    Logger.getLogger("org").setLevel(Level.WARN)
    Logger.getLogger("edu").setLevel(Level.WARN)
    println("loading models...")
    val sc = SparkContext.getOrCreate(
      new SparkConf()
        .setMaster("local[2]")
        .setAppName(this.getClass.getSimpleName)
    )
    RelationExtractor.init()

    println("Initilization finished:")

    companyList.foreach { company =>
      if (company.nonEmpty) Try {
        getResult(company, sc)
      }
    }

  }

  private def getResult(company: String, sc: SparkContext): DataFrame = {
    val rawTextFile = s"$textPath/page-${company}_0.txt"
    val extractedDF = SparkBatchDriver.processTextFiles(sc.textFile(rawTextFile))

    val labelFile = s"$labelPath/${company}.csv"
    val relationRDD = sc.textFile(labelFile).filter(!_.startsWith("//")).filter(_.nonEmpty).map { line =>
      val elements = line.split("\t")
      RelationLine(elements(0), elements(1), elements(2), elements(3))
    }
    val sqlContext = SQLContext.getOrCreate(sc)
    val labelledDF = sqlContext.createDataFrame(relationRDD)

    val correctDF = labelledDF.intersect(extractedDF)
    println("correct:")
    correctDF.show(false)

    println("missed:")
    labelledDF.except(correctDF).show(false)

    println("wrong:")
    extractedDF.except(correctDF).show(false)

    val recall = correctDF.count().toDouble / labelledDF.count()
    val precision = correctDF.count().toDouble / extractedDF.count()
    println(s"recall: $recall. precision: $precision")

    labelledDF
  }


  val textPath = "data/evaluation/web"
  val labelPath = "data/evaluation/extraction"
  val companyList = Array("Apple")




}




