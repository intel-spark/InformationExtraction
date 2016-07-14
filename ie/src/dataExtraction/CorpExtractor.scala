package dataExtraction

import java.io.PrintWriter

import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ArrayBuffer

/**
 * Created by yuhao on 6/29/16.
 */
object CorpExtractor {

  def main(args: Array[String]) {
    val sc = SparkContext.getOrCreate(
      new SparkConf()
        .setMaster("local[2]")
        .setAppName(this.getClass.getSimpleName)
    )

    val text = sc.textFile("data/conll04.corp").collect()
    val dataset = new ArrayBuffer[(Double, String)]()

    var i = 0
    while ( i < text.length) {
      var str = ""
      while(text(i).trim.nonEmpty){
        str += text(i).split("\t")(5) + " "
        i += 1
      }

      i += 1
      var label = 0
      while(text(i).trim.nonEmpty){
        if(text(i).contains("Work_For")) label = 1
        i += 1
      }

      dataset.append((label, str))
      i += 1
    }

    println(dataset.count(_._1 == 1.0))
    println(dataset.length)

    val pw = new PrintWriter("data/sentences.txt")
    dataset.foreach { t =>
      pw.println(t._1 + "::::::::" + t._2)
    }
    pw.close()

  }

}
