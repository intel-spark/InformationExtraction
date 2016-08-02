package evaluation

import java.io.File

import Test.RegexNerTest
import intel.analytics.KBPModel

import scala.collection.JavaConverters._
import scala.io.Source
import evaluation.preparation.label.Label.{organization, person, title}

/**
  * Created by xianyan on 7/27/16.
  */
object NerEvaluationTest {

  private def processSentence(line: String): Unit = {
    println(RegexNerTest.extractNER(line).asScala.mkString(", "))
    KBPModel.extract(line).asScala.foreach(t => println(t._1))
  }

  def main(args: Array[String]): Unit = {

    var tp = 0
    var fp = 0
    var fn = 0
    var tn = 0

    val files = new File("data/evaluation/labeled").listFiles()
    var res = List(List("Company", "Precision", "Recall", "F1"))
    println("please input the entity type you want to test, separated by \",\", \ne.g. PERSON, TITLE\n>")
    val entityTpes = scala.io.StdIn.readLine()
    for (labeledFile <- files) {
      val fileName = labeledFile.getName
      val company = fileName.substring(fileName.indexOf("-")+1, fileName.indexOf("."));
      val lines = Source.fromFile(EvalPaths.webContentPath(company, 0)).getLines().toList
      val nerList = for (line <- lines)
        yield RegexNerTest.extractNER(line).asScala.mkString("\t")

      val ner = new NerEvaluation()


      println("Evaluate for company: " +company)
      ner.eval(nerList, ner.transformLabelFromFile(labeledFile.getAbsolutePath),
        entityTpes.toUpperCase(), ",", ner.transformTextFromFile(labeledFile.getAbsolutePath))


//      println("\n\nEvaluation result for company " + company)
      res  :+= List(company, ner.precision.formatted("%.3f"), ner.recall.formatted("%.3f"), ner.f1.formatted("%.3f"))
      tp += ner.tp
      fp += ner.fp
      fn += ner.fn
      tn += ner.tn
    }
    val ner = new NerEvaluation(tp, fp, fn, tn)
    res :+= List("Overall", ner.precision.formatted("%.3f"),
      ner.recall.formatted("%.3f"),
      ner.f1.formatted("%.3f"))
    println(Tabulator.format(res))
  }

}
