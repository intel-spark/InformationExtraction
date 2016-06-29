import Relation.RelationExtractor
import feature.{functions, CoreNLP}
import functions._
import feature.CoreNLP
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.functions._
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.functions._
import functions._

/**
 * Created by yuhao on 6/29/16.
 */
object Driver {

  def main(args: Array[String]) {
    val sc = SparkContext.getOrCreate(
      new SparkConf()
        .setMaster("local[6]")
        .setAppName(this.getClass.getSimpleName)
    )
    RelationExtractor.init()
    val data = getDataset(sc)

    getPipelineResult(data)
    getWorkRelation(data)
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

  private def getWorkRelation(data: RDD[(Double, String)]): Unit ={
    data.foreach { s =>
      val map = RelationExtractor.extract(s._2)
      if(!map.isEmpty) println(map)
    }
  }

  private def getDataset(sc: SparkContext): RDD[(Double, String)] ={
    sc.textFile("data/sample.txt", 6).map { s =>
      val t = s.split("::::::::")
      (t(0).toDouble, t(1))
    }

  }

}
