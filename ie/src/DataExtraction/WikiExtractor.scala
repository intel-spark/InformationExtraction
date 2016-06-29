package DataExtraction

import java.io.PrintWriter

import feature.functions
import functions._
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.functions._
import org.apache.spark.{SparkConf, SparkContext}

import scala.io.Source

/**
 * Created by yuhao on 6/29/16.
 */
object WikiExtractor {

  def main(args: Array[String]) {

    val validLines = Source.fromFile("data/origin/wikipedia.train").getLines().filter(_.contains("relation=\"employer\""))

    val sc = SparkContext.getOrCreate(
      new SparkConf()
        .setMaster("local[2]")
        .setAppName(this.getClass.getSimpleName)
    )
    val sqlContext = SQLContext.getOrCreate(sc)
    import sqlContext.implicits._
    val input = sqlContext.createDataFrame(validLines.zipWithIndex.toSeq).toDF("text", "id")

    val output = input.select(cleanxml('text).as('doc))

    val pw = new PrintWriter("data/wiki.txt")
    output.collect().map(r => r.getString(0)).foreach(s => pw.println("1.0::::::::" + s))
    pw.close()

  }

}
