package DataExtraction

import java.io.PrintWriter

import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ArrayBuffer

/**
 * Created by yuhao on 6/29/16.
 */
object TitleExtractor {

  def main(args: Array[String]) {
    val sc = SparkContext.getOrCreate(
      new SparkConf()
        .setMaster("local[2]")
        .setAppName(this.getClass.getSimpleName)
    )

    val text = sc.textFile("data/SampleofReportedTitles.txt").collect()
    val titles = text.map(s => s.split("\t")(1))
      .flatMap(_.split("\\(").map(_.replaceAll("\\)", "").trim))

    val pw = new PrintWriter("data/titles")
    titles.foreach(pw.println(_))


  }

}
