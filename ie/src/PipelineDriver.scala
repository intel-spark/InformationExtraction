import feature.functions._
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.functions._

/**
  * Created by yuhao on 7/13/16.
  */
object PipelineDriver {

  def main(args: Array[String]) {
    val sc = SparkContext.getOrCreate(
      new SparkConf()
        .setMaster("local[6]")
        .setAppName(this.getClass.getSimpleName)
    )

    val sentences = Seq(
      "Jason Vice President",
      "Yuhao Vice President"
    )

    val rdd = sc.parallelize(sentences).cache()
    getPipelineResult(rdd)
  }


  private def getPipelineResult(data: RDD[String]): Unit ={
    val sqlContext = SQLContext.getOrCreate(data.sparkContext)
    import sqlContext.implicits._
    val input = sqlContext.createDataFrame( data.zipWithIndex()
    ).toDF("text", "id")

    val output = input.select(cleanxml('text).as('doc))
      .select(explode(ssplit('doc)).as('sen))
      .select('sen, tokenize('sen).as('words),
        ner('sen).as('nerTags),
        openie('sen).as('openie)
        //        ,coref('sen).as('coref)
      )
    output.show(truncate = false)
  }
}
