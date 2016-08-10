package evaluation.preparation.crawl

import java.io.{BufferedWriter, FileWriter}
import evaluation.EvalPaths

import scala.io.Source
import scala.reflect.io.File
import scala.collection.JavaConversions._
import scala.util.control.Exception._
/**
  * Created by xianyan on 7/28/16.
  */
object CleanTest {

  def webContentPath(label: String, i: Int): String = {
    val root = "data/evaluation/web4/"
    val dir = File(s"${root}${label}/")
    if(!dir.exists) dir.createDirectory()
    s"${root}$label/page-${label}_${i}.txt"
  }

  def webSavePath(label: String, i: Int): String = {
    val root = "data/evaluation/web/"
    val dir = File(s"${root}${label}/")
    if(!dir.exists) dir.createDirectory()
    s"${root}$label/page-${label}_${i}.txt"
  }

  def reviseLine(line: String): String =
    line.replaceAll("^(\\s*,\\s*)+","").replaceAll("(\\s*,\\s*)+", ", ")

  
  
  def main(args: Array[String]): Unit = {

//    println(reviseLine(", ,hello world,,,, fg,fg"))
    val crawler = new Crawler()
//    while (true) {
      val urlMap = EvalPaths.urlMap()
      for ((label, urlStr) <- urlMap) {
        println(webContentPath(label, 0))
        if(File(webContentPath(label, 0)).exists) {
          val newtext = Source.fromFile(webContentPath(label, 0)).getLines().map(line => {
            reviseLine(line)
          }).mkString("\n")

          val writer = new BufferedWriter(new FileWriter(webSavePath(label, 0)))
          println(newtext)
          writer.write(newtext)
          writer.close()
        }

      }
//    }

  }

}
