package evaluation

import Test.RegexNerTest
import intel.analytics.KBPModel

import scala.collection.JavaConverters._
import scala.io.Source

/**
  * Created by xianyan on 7/27/16.
  */
object NerEvaluationTest {

  private def processSentence(line: String): Unit = {
    println(RegexNerTest.extractNER(line).asScala.mkString(", "))
    KBPModel.extract(line).asScala.foreach(t => println(t._1))
  }

  def main(args: Array[String]): Unit = {

    val lines = Source.fromFile("data/evaluation/web/web-content-Walmart_0.txt").getLines().toList
    var nerList = List[String]()
    for (line <- lines) {
      nerList :+= RegexNerTest.extractNER(line).asScala.mkString(", ")
    }
    val ner = new NerEvaluation()
    ner.eval(nerList, ner.transformFromFile("data/evaluation/labeled/labeled-Walmart.txt"), "PERSON, TITLE", ",", lines)
    println(ner.precision)
    println(ner.recall)
  }

}
