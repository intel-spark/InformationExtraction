import dataExtraction.DirectExtraction
import relation.RelationExtractor
import feature.{CoreNLP, functions}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.functions._
import functions._
import intel.analytics.KBPModel

case class RelationLine(
                         name:String,
                         title:String,
                         org: String,
                         text: String)

object SparkBatchTest {

  def main(args: Array[String]) {
    val sc = SparkContext.getOrCreate(
      new SparkConf()
        .setMaster("local[6]")
        .setAppName(this.getClass.getSimpleName)
    )
    RelationExtractor.init()
    val data = getDataset(sc)

    val relations = getWorkRelation(data)
    relations.show(false)
  }

  private def getWorkRelation(data: RDD[String]): DataFrame ={
    val relations = data.map { s =>
      val relations = KBPModel.extract(s)
      var name: Option[String] = None
      var title: Option[String] = None
      var organization: Option[String] = None
      var department: Option[String] = None
      relations.foreach { r =>
        if(r.relationGloss().trim == "per:title"){
          val per = r.subjectGloss()
          name = Some(per)
          title = Some(r.objectGloss())
        }
        if(r.relationGloss().trim == "per:employee_of"){
          val per = r.subjectGloss()
          name = Some(per)
          organization = Some(r.objectGloss())
        }
        if(r.relationGloss().trim == "org:top_members/employees"){
          val per = r.objectGloss()
          name = Some(per)
          organization = Some(r.subjectGloss())
        }
      }

      val directResult = DirectExtraction.extract(s)
      if(directResult != null){
        name = Some(directResult._1)
        title = Some(directResult._2)
        department = Some(directResult._3)
      }

      RelationLine(name.getOrElse("null"), title.getOrElse("null"), organization.getOrElse("null"), s)
    }

    val sqlContext = SQLContext.getOrCreate(data.sparkContext)
    sqlContext.createDataFrame(relations)
  }

  private def getDataset(sc: SparkContext): RDD[String] = {
    val rdd = sc.textFile("data/sample.txt", 12).cache()
    rdd
  }

}

