package org.apache.spark.sql

import java.io.File

import com.intel.ie.relation.RelationExtractor
import com.intel.ie.{RelationLine, SparkBatchDriver}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.functions._
import org.apache.spark.{SparkConf, SparkContext}

case class RelationRow(
   company: String,
   name: String,
   relation: String,
   entity: String,
   text: String
)


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
    sc.hadoopConfiguration.set("mapreduce.input.fileinputformat.input.dir.recursive","true")
    
    val sqlContext = SQLContext.getOrCreate(sc)
    val st = System.nanoTime()
    val extractionResult = sc.wholeTextFiles(textPath, 8)
      .filter { case (title, content) =>
        val companyName = new File(new File(title).getParent).getName
        companyList.contains(companyName)  
      }.flatMap { case (title, content) => 
        val companyName = new File(new File(title).getParent).getName
        content.split("\n")
          .map(line => if (line.length > 500) line.substring(0, 500) else line)
          .flatMap(line => SparkBatchDriver.getWorkRelation(line))
          .map(rl => (companyName, rl))
          .map(t => RelationRow(t._1, t._2.name, t._2.relation, t._2.entity, t._2.text))
      }
    val extractedDF = sqlContext.createDataFrame(extractionResult).cache()    
    
    val labelledResult = sc.wholeTextFiles(labelPath, 8)
      .filter { case (title, content) =>
        val companyName = new File(new File(title).getParent).getName
        companyList.contains(companyName)
      }
      .flatMap { case (title, content) =>
      val companyName = new File(new File(title).getParent).getName
      content.split("\n").filter(_.nonEmpty).map { line =>
          val elements = line.replaceAll("\u00a0", " ").replaceAll("\u200B|\u200C|\u200D|\uFEFF", "").replace("  ", " ").split("\t")
          RelationRow(companyName, elements(0), elements(1), elements(2), elements(3))
        }
    }
    val labelledDF = sqlContext.createDataFrame(labelledResult).cache()

    getResultForOneRelation("all", extractedDF, labelledDF, "title", sc)
    println((System.nanoTime() - st) / 1e9 + " seconds")
    
    if(args(0) == "-d"){
      val pageResults = companyList.map { companyName =>
        val extractedFiltered = extractedDF.where(col("company") === companyName).cache()
        val labelledFiltered = labelledDF.where(col("company") === companyName).cache()
        val pageResult = getResultForOneRelation(companyName, extractedFiltered, labelledFiltered, "title", sc)
        extractedFiltered.unpersist()
        labelledFiltered.unpersist()
        pageResult
      }.toSeq.toArray

      println(s"Overall results: ${pageResults.size} companies evaluated")
      def printResultForOneRelation(relation: String, index: Int): Unit = {
        SQLContext.getOrCreate(sc).createDataFrame(sc.parallelize(pageResults.map(_(index)))).show(300, false)
        val totalCorrect = pageResults.map(_ (index).correct).sum
        val totalWrong = pageResults.map(_ (index).wrong).sum
        val totalMissed = pageResults.map(_ (index).missed).sum
        val recall = totalCorrect.toDouble / (totalCorrect + totalMissed)
        val precision = totalCorrect.toDouble / (totalCorrect + totalWrong)
        println(s"overall result for $relation --- recall: $recall. precision: $precision")
      }
      printResultForOneRelation("title", 0)
    }


  }
  

  def getResultForOneRelation(company: String, extractedRDD: DataFrame, relationRDD: DataFrame, relationType: String, sc: SparkContext): Array[PageResult] = {
    val extractedRawDF = extractedRDD.where(col("relation").isin(relationType))
    val extractedLowerDF = extractedRawDF
      .select(lower(extractedRawDF("name")).alias("name"), extractedRawDF("relation"), lower(extractedRawDF("entity")).alias("entity"), extractedRawDF("text"))
    val extractedDF = extractedLowerDF
      .select("name", "relation", "entity")
      .distinct()
      .cache()


    val labelledRawDF = relationRDD.where(col("relation").isin(relationType))
    val labelledLowDF = labelledRawDF
      .select(lower(labelledRawDF("name")).alias("name"), labelledRawDF("relation"), lower(labelledRawDF("entity")).alias("entity"), labelledRawDF("text"))
    val labelledDF = labelledLowDF
      .select("name", "relation", "entity")
      .distinct()
      .cache()

    val correctDF = labelledDF.intersect(extractedDF).distinct().cache()
    println(company)
    println(Console.BLUE + "correct:")
    println(correctDF.showString(100, false))

    println(Console.RED + "missed:")
    println(labelledDF.except(correctDF).join(labelledLowDF, Seq("name", "relation", "entity"))
      .distinct().showString(100, false))

    println("wrong:")
    println(Console.RED + extractedDF.except(correctDF).join(extractedLowerDF, Seq("name", "relation", "entity"))
      .distinct().showString(100, false))

    val extractedCt = extractedDF.count()
    val labelledCt = labelledDF.count()
    val correctCt = correctDF.count()
    val recall = correctCt.toDouble / labelledCt
    val precision = correctCt.toDouble / (if (extractedCt == 0) 1 else extractedCt)
    println(Console.YELLOW_B + s"recall: $recall. precision: $precision. (" +
      s"extracted: ${extractedCt}; labelled: ${labelledCt}; correct: ${correctCt})")
    println(Console.RESET)
    
    labelledDF.unpersist()
    extractedDF.unpersist()
    Array(PageResult(company, extractedCt, labelledCt, correctCt, extractedDF.count() - correctCt, labelledDF.count() - correctCt))
  }

  val textPath = "data/evaluation/web/"
  val labelPath = "data/evaluation/extraction"
  val partitionSize = 12
  val companyList = //Array("Apple", "Alcoa") 
    new File("data/evaluation/extraction").listFiles().map(f => f.getName).sorted
}

case class PageResult(company: String, extracted: Long, labelled: Long, correct: Long, wrong: Long, missed: Long)

