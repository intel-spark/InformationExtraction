import java.io.File

import intel.analytics.KBPModel
import org.apache.log4j.{Level, Logger}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}
import relation.RelationExtractor

import scala.collection.JavaConverters._
import scala.util.Try

case class RelationLine(
  name:String,
  relation:String,
  entity: String,
  text: String)

object SparkBatchTest {

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

    Iterator.continually(scala.io.StdIn.readLine("dataset path>")).foreach { line =>
      if (line.nonEmpty) Try {
        if(new File(line).exists()){
          val data = getDataset(sc, line)
          if(line.endsWith(".csv")){
            processCSVFiles(data)
          } else {
            processTextFiles(data)
          }
        } else {
          processSentence(line)
        }
      }
    }
  }


  private def processTextFiles(data: RDD[String]): Unit ={
    val relations = getWorkRelation(data)
    relations.show(100, false)
    print("dataset path>")
  }

  private def processCSVFiles(data: RDD[String]): Unit ={
    val text = data.filter(!_.startsWith("//"))map(s =>
      s.split('|')(3)
    )
    val relations = getWorkRelation(text)
    relations.show(100, false)
    print("dataset path>")
  }

  private def processSentence(line: String): Unit ={
    KBPModel.extract(line).asScala.foreach(t => println(t._1))
//    val relations = getWorkRelation(text)
//    relations.show(100, false)
//    print("dataset path>")
  }



  private def getWorkRelation(data: RDD[String]): DataFrame ={
    val relations = data.flatMap { s =>
      val raw = KBPModel.extract(s)
      raw.asScala.toSeq.map { case (r, sen) =>
        if(r.relationGloss() == "org:top_members/employees") {
          RelationLine(r.objectGloss(), "top member of", r.subjectGloss(), sen)
        } else {
          RelationLine(r.subjectGloss(), r.relationGloss().split(":")(1), r.objectGloss(), sen)
        }
      }
    }

    val sqlContext = SQLContext.getOrCreate(data.sparkContext)
    sqlContext.createDataFrame(relations)
  }

  private def getDataset(sc: SparkContext, path: String): RDD[String] = {

    val rdd = sc.textFile(path)
    rdd.unpersist(true)
    rdd.count()
    rdd
  }

}

