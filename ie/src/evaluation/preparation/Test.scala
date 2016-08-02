package evaluation.preparation

import evaluation.NerEvaluation
import scala.io.Source
/**
  * Created by xianyan on 8/2/16.
  */
object Test {
  private def transformText(line: String): String = {
    val items = line.split("\t")
    val tranStr = for (item <- items; eles = item.split("/") if (eles.length > 1)) yield eles(0).trim
    tranStr.mkString(", ")
  }
  private def transformLabel(line: String): String = {
    val items = line.split("\t")
    val tranStr = for (item <- items; eles = item.split("/") if (eles.length > 1)) yield eles(1)
    tranStr.mkString(", ")
  }
  def main(args: Array[String]): Unit = {
//    val ner = new NerEvaluation()
//    val labels = ner.transformLabelFromFile("data/evaluation/labeled/labeled-Apple.txt").toList
//    val texts = ner.transformTextFromFile("data/evaluation/labeled/labeled-Apple.txt").toList
//    for ((label, text) <- labels zip texts) {
//      val as = label.split(",").map(_.trim).map(_.toLowerCase).toList
//      val bs = text.split(",").map(_.trim).map(_.toLowerCase).toList
//      if (as.length != bs.length) {
//        println(as +"\n"+ bs)
//      }
//    }
    val line = "Tim/PERSON\tCook/PERSON\t,/O\tCEO/TITLE"
    println(transformText(line))
    println(transformLabel(line))
  }
}
