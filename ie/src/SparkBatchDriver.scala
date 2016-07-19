import dataExtraction.DirectExtraction
import edu.stanford.nlp.io.IOUtils
import relation.RelationExtractor
import feature.{CoreNLP, functions}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import intel.analytics.KBPModel
import org.apache.log4j.{Level, Logger}
import scala.collection.JavaConverters._

import scala.io.Source
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
    print("dataset path>")
    Iterator.continually(Console.readLine).foreach { line =>
      Try {
        val data = getDataset(sc, line)
        processTextFiles(data)
      }
      print("dataset path>")
    }
  }


  private def processTextFiles(data: RDD[String]): Unit ={
    val relations = getWorkRelation(data)
    relations.show(false)
    print("dataset path>")
  }
  
  private def getWorkRelation(data: RDD[String]): DataFrame ={
    val relations = data.flatMap { s =>
      val raw = KBPModel.extract(s)
      raw.asScala.toSeq.map { case (r, sen) =>
        if(r.relationGloss() == "org:top_members/employees") {
          RelationLine(r.objectGloss(), "at top of", r.subjectGloss(), sen)
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
    rdd.count()
    rdd
  }

}

