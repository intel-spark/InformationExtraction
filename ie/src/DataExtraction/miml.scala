package DataExtraction

import java.io.PrintWriter

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by yuhao on 7/8/16.
  */
class miml {

  def main(args: Array[String]) {

    val sc = SparkContext.getOrCreate(
      new SparkConf()
        .setMaster("local[2]")
        .setAppName(this.getClass.getSimpleName)
    )

    val text = sc.textFile("/home/yuhao/workspace/github/hhbyyh/InformationExtraction/ie/data/miml/mimlre-2014-07-17-data/annotated_sentences.csv").collect()
    val titles = text.filter(_.startsWith())

    val pw = new PrintWriter("data/titles")
    titles.foreach(pw.println(_))

  }

}
