package org.apache.spark.sql

import java.io.File

import com.intel.ie.relation.RelationExtractor
import com.intel.ie.{RelationLine, SparkBatchDriver}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.functions._
import org.apache.spark.{SparkConf, SparkContext}

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
    println(s"Overall results: ${pageResults.size} companies evaluated")
    def printResultForOneRelation(relation: String, index: Int): Unit = {
      SQLContext.getOrCreate(sc).createDataFrame(sc.parallelize(pageResults.map(_ (index)))).show(300, false)
      val totalCorrect = pageResults.map(_ (index).correct).sum
      val totalWrong = pageResults.map(_ (index).wrong).sum
      val totalMissed = pageResults.map(_ (index).missed).sum
      val recall = totalCorrect.toDouble / (totalCorrect + totalMissed)
      val precision = totalCorrect.toDouble / (totalCorrect + totalWrong)
      println(s"overall result for $relation --- recall: $recall. precision: $precision")
    }
    printResultForOneRelation("title", 0)
    println("\n")
    printResultForOneRelation("employee_of", 1)
  }

  private def getResult(company: String, sc: SparkContext): Array[PageResult] = {
    val rawTextFile = s"$textPath/${company}/page-${company}_0.txt"


    val extractedRDD = SparkBatchDriver.processTextFiles(sc.textFile(rawTextFile).map {
      line => if (line.length > 500) line.substring(0, 500) else line
    }.map(_.replaceAll("\u00a0", " ")
      //zero space
      .replaceAll("\u200B|\u200C|\u200D|\uFEFF", "")))

    val labelFile = s"$labelPath/${company}/page-${company}_0.txt"
    val relationRDD = sc.textFile(labelFile).filter(!_.startsWith("//")).filter(_.nonEmpty).map { line =>
      val elements = line.replaceAll("\u00a0", " ").replaceAll("\u200B|\u200C|\u200D|\uFEFF", "").replace("  ", " ").split("\t")
      RelationLine(elements(0), elements(1), elements(2), elements(3))
    }


    def getResultForOneRelation(relationType: String): PageResult = {
      val extractedRawDF = extractedRDD.where(col("relation").isin(relationType))
        .cache()

      val extractedDF = extractedRawDF
        .select("name", "relation", "entity")
        .distinct()
        .cache()

      val sqlContext = SQLContext.getOrCreate(sc)
      val labelledRawDF = sqlContext.createDataFrame(relationRDD)
        .where(col("relation").isin(relationType))
        .cache()
      val labelledDF = labelledRawDF
        .select("name", "relation", "entity")
        .distinct()
        .cache()

      val correctDF = labelledDF.intersect(extractedDF).distinct().cache()
      println(company)
      println(Console.BLUE + "correct:")
      println(correctDF.showString(100, false))

      println(Console.RED + "missed:")
      println(labelledDF.except(correctDF).join(labelledRawDF, Seq("name", "relation", "entity"))
        .distinct().showString(100, false))

      println("wrong:")
      println(Console.RED + extractedDF.except(correctDF).join(extractedRawDF, Seq("name", "relation", "entity"))
        .distinct().showString(100, false))

      val extractedCt = extractedDF.count()
      val labelledCt = labelledDF.count()
      val correctCt = correctDF.count()
      val recall = correctCt.toDouble / labelledCt
      val precision = correctCt.toDouble / (if (extractedCt == 0) 1 else extractedCt)
      println(Console.YELLOW_B + s"recall: $recall. precision: $precision. (" +
        s"extracted: ${extractedCt}; labelled: ${labelledCt}; correct: ${correctCt})")
      println(Console.RESET)

      PageResult(company, extractedCt, labelledCt, correctCt, extractedDF.count() - correctCt, labelledDF.count() - correctCt)
    }
    
    Array(getResultForOneRelation("title"), getResultForOneRelation("employee_of"))
  }

  val textPath = "data/evaluation/web"
  val labelPath = "data/evaluation/extraction"
  val companyList =
//    Array("Emerson Electric")
//      Array("A-Mark Precious Metals", "Avis Budget Group", "Barnes & Noble", "Cigna", "US Foods", "Computer Sciences", "Crown Holdings", "Emerson Electric", "Kelly Services", "Kinder Morgan", "NRG Energy")
//    new File("data/evaluation/extraction").listFiles().map(f => f.getName).sorted
      new File("data/evaluation/extraction").listFiles().map(f => f.getName).filter(filename => filename > "Disney" && filename <= "L Brands").sorted

}

case class PageResult(company: String, extracted: Long, labelled: Long, correct: Long, wrong: Long, missed: Long)

