package DataExtraction

import java.io.PrintWriter

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by yuhao on 7/8/16.
  */
object miml {

  def main(args: Array[String]) {

    val sc = SparkContext.getOrCreate(
      new SparkConf()
        .setMaster("local[2]")
        .setAppName(this.getClass.getSimpleName)
    )

    val text = sc.textFile("/home/yuhao/workspace/github/hhbyyh/InformationExtraction/ie/data/miml/mimlre-2014-07-17-data/annotated_sentences.csv").collect()
    val works = text
      .filter(!_.startsWith("\"key\",\"relation\",\"confidence\","))
      .filter(_.contains("per:employee_of"))
      .map(_.split(",")(4))
      .foreach(println(_))


//    val pw = new PrintWriter("data/titles")
//    titles.foreach(pw.println(_))

  }

}
