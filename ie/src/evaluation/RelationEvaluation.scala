package evaluation

import java.io.File

import main.{RelationLine, SparkBatchDriver}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}
import relation.RelationExtractor
import org.apache.spark.sql.functions._

object RelationEvaluation {

  def main(args: Array[String]) {
    Logger.getLogger("org").setLevel(Level.WARN)
    Logger.getLogger("intel").setLevel(Level.WARN)
    Logger.getLogger("edu").setLevel(Level.WARN)
    println("loading models...")
    val sc = SparkContext.getOrCreate(
      new SparkConf()
        .setMaster("local[6]")
        .setAppName(this.getClass.getSimpleName)
    )
    RelationExtractor.init()

    val pageResults = companyList.toSeq.filter(_.nonEmpty).map { company =>
      getResult(company, sc)
    }

    val totalCorrect = pageResults.map(_.correct).sum
    val totalWrong = pageResults.map(_.wrong).sum
    val totalMissed = pageResults.map(_.missed).sum
    val recall = totalCorrect.toDouble / (totalCorrect + totalMissed)
    val precision = totalCorrect.toDouble / (totalCorrect + totalWrong)
    println(s"overall recall: $recall. precision: $precision")
  }

  private def getResult(company: String, sc: SparkContext): PageResult = {
    val rawTextFile = s"$textPath/${company}/page-${company}_0.txt"
    val extractedDF = SparkBatchDriver.processTextFiles(sc.textFile(rawTextFile).filter(_.length < 200))
      .where(col("relation").isin("title"))
      .select("name", "relation", "entity")
      .distinct()
      .cache()
    extractedDF.show()

    val labelFile = s"$labelPath/${company}/page-${company}_0.txt"
    val relationRDD = sc.textFile(labelFile).filter(!_.startsWith("//")).filter(_.nonEmpty).map { line =>
      val elements = line.split("\t")
      RelationLine(elements(0), elements(1), elements(2), elements(3))
    }
    
    val sqlContext = SQLContext.getOrCreate(sc)
    val labelledDF = sqlContext.createDataFrame(relationRDD)
      .where(col("relation").isin("title"))
      .select("name", "relation", "entity")
      .distinct()
      .cache()


    val correctDF = labelledDF.intersect(extractedDF).cache()
    println(company)
    println("\tcorrect:")
    correctDF.show(false)

    println("\tmissed:")
    labelledDF.except(correctDF).show(false)

    println("\twrong:")
    extractedDF.except(correctDF).show(false)

    val recall = correctDF.count().toDouble / labelledDF.count()
    val precision = correctDF.count().toDouble / extractedDF.count()
    println(s"recall: $recall. precision: $precision")
    val correctCt = correctDF.count()
    PageResult(correctCt, extractedDF.count() - correctCt, labelledDF.count() - correctCt)
  }

  val textPath = "data/evaluation/web"
  val labelPath = "data/evaluation/extraction"
  val companyList = //Array("Apple", "AGL Resources")
    new File("data/evaluation/extraction").listFiles().map(f => f.getName).sorted

}

case class PageResult(correct: Long, wrong: Long, missed: Long)




