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
    println("0.8 | CNBC | http://www.cnbc.com/2016/06/06/wal-mart-ceo-theres-still-room-to-grow-us-sales.html | Doug McMillon of Wal-Mart. A return to retail basics has been a key driver behind the recent momentum at Wal-Mart U.S., including its first-quarter revenue beat.Yet as the world's largest retailer moves through these fundamentally simple fixes, which include cleaning up its stores and shortening the wait time at checkout, CEO Doug McMillon told CNBC there are more opportunities to improve its in-store operations.One of the most promising of those opportunities, McMillon said, is trimming the amount of inventory it has on hand. By doing so, it can declutter the backroom of its stores (thereby speeding up the amount of time it takes a worker to locate certain products), and focus on keeping key items in stock. \"There's still a lot of upside as it relates to that, and that's kind of like oxygen to a store,\" McMillon said.".split('|').mkString("\n"))
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
        if(line.endsWith(".csv")){
          processCSVFiles(data)
        } else {
          processTextFiles(data)
        }
      }
      print("dataset path>")
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

