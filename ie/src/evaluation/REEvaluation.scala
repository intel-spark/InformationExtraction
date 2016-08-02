package evaluation

import java.io.File

import main.{RelationLine, SparkBatchDriver}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}
import relation.RelationExtractor

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
        .setMaster("local")
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
    val extractionResult = SparkBatchDriver.processTextFiles(sc.textFile(rawTextFile))

    extractionResult.show(false)

    val labelFile = s"$labelPath/${company}.csv"
    val relationRDD = sc.textFile(labelFile).filter(!_.startsWith("//")).filter(_.nonEmpty).map { line =>
      val elements = line.split("\t")
      RelationLine(elements(0), elements(1), elements(2), elements(3))
    }

    val sqlContext = SQLContext.getOrCreate(sc)
    val labelDataFrame = sqlContext.createDataFrame(relationRDD)
    println("labeled: ")
    labelDataFrame.show(false)

    val labeledTitles = labelDataFrame.where("relation=title").collect()
    val labeled = labeledTitles.map { r =>
        r.getString(0) + ", " + r.getString(1) + ", " + r.getString(2)
      }.toSet

    println(labeled.mkString("\n"))
    val extracted = extractionResult.where("relation=title").rdd.map(r => (0 to 2).map(i => r.getString(i)).mkString(", ")).collect().toSet

    val correct = labeled.union(extracted).size
    val recall = correct.toDouble / labeled.size
    val precision = correct.toDouble / extracted.size
    println(s"recall: $recall. precision: $precision")

    labelDataFrame


  }


  val textPath = "data/evaluation/web"
  val labelPath = "data/evaluation/extraction"
  val companyList = Array("Apple")




}




