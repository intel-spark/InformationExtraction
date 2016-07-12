import Relation.RelationExtractor
import edu.stanford.blp.ie.KBPModel
import feature.{CoreNLP, functions}
import functions._
import feature.CoreNLP
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.functions._
import functions._

object Driver {

  def main(args: Array[String]) {
    val sc = SparkContext.getOrCreate(
      new SparkConf()
        .setMaster("local[6]")
        .setAppName(this.getClass.getSimpleName)
    )
    RelationExtractor.init()
    val data = getDataset(sc)

//    getPipelineResult(data)
//    getWorkRelation(data)

    val relations = getWorkRelation(data)
    relations.show(false)
  }

  private def getPipelineResult(data: RDD[(Double, String)]): Unit ={
    val sqlContext = SQLContext.getOrCreate(data.sparkContext)
    import sqlContext.implicits._
    val input = sqlContext.createDataFrame( data.map(_._2).zipWithIndex()
    ).toDF("text", "id")

    val output = input.select(cleanxml('text).as('doc))
      .select(explode(ssplit('doc)).as('sen))
      .select('sen, tokenize('sen).as('words),
        ner('sen).as('nerTags),
        sentiment('sen).as('sentiment),
        openie('sen).as('openie)
        //        ,coref('sen).as('coref)
      )
    output.show(truncate = false)
  }


  private def getWorkRelation(data: RDD[(Double, String)]): DataFrame ={
    val relations = data.map { s =>
      val text = s._2
      val relations = KBPModel.extract(s._2)
      var name: Option[String] = None
      var title: Option[String] = None
      var organization: Option[String] = None
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
      RelationLine(name.getOrElse("null"), title.getOrElse("null"), organization.getOrElse("null"), s._2)
    }

    val sqlContext = SQLContext.getOrCreate(data.sparkContext)
    import sqlContext.implicits
    sqlContext.createDataFrame(relations)
  }

  private def getDataset(sc: SparkContext): RDD[(Double, String)] = {
//    sc.textFile("data/sample.txt", 6).map { s =>
//      val t = s.split("::::::::")
//      (t(0).toDouble, t(1))
//    }
    val rdd = sc.textFile("data/miml/work_for", 12).sample(false, 0.1).map(s => (1.0, s)).cache()
    rdd
  }

}

case class RelationLine(
  name:String,
  title:String,
  org: String,
  text: String) {

}