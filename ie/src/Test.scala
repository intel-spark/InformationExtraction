/**
 * Created by yuhao on 6/9/16.
 */

import feature.{functions, CoreNLP}
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.functions._
import functions._



object Test {

  def main(args: Array[String]) {

    val conf = new SparkConf().setMaster("local[2]").setAppName("lll")
    val sc = SparkContext.getOrCreate(
      new SparkConf()
        .setMaster("local[2]")
        .setAppName(this.getClass.getSimpleName)
    )
    val sqlContext = SQLContext.getOrCreate(sc)
    import sqlContext.implicits._
    val input = sqlContext.createDataFrame(Seq(
      (1, "<xml>Stanford University is located in California. It is a great university.</xml>"),
      (2, "<xml>Steve Jobs is the CEO of Apple.</xml>"),
      (3, "<xml>it is said by the CEO of Microsoft, Yuhao Yang.</xml>"),
      (4, "Stacy J. Smith is executive vice president and chief financial " +
        "officer (CFO) for Intel Corporation.")
    )).toDF("id", "text")

    val output = input.select(cleanxml('text).as('doc))
      .select(explode(ssplit('doc)).as('sen))
      .select('sen, tokenize('sen).as('words),
        ner('sen).as('nerTags),
        sentiment('sen).as('sentiment),
        openie('sen).as('openie)
//        ,coref('sen).as('coref)
      )

    output.printSchema()


    output.show(truncate = false)
  }

}
