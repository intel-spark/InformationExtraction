package com.intel.ie.evaluation

import java.io.File

import com.intel.ie.RegexNerTest
import com.intel.ie.analytics.KBPModel
import com.intel.ie.analytics._

import scala.collection.JavaConverters._
import scala.io.Source
import com.intel.ie.evaluation.preparation.label.Label.{organization, person, title}

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
    val entityTpes = IOUtils.readLine()
    for (labeledFile <- files) {
      val fileName = labeledFile.getName
      val company = fileName.substring(fileName.indexOf("-")+1, fileName.indexOf("."));
      val lines = Source.fromFile(EvalPaths.webContentPath(company, 0)).getLines().toList
      val nerList = for (line <- lines)
        yield RegexNerTest.extractNER(line).asScala.mkString("\t")

      val ner = new NerEvaluation()

      println()
      println("Evaluate for company: " + company)
      ner.eval(nerList, ner.transformLabelFromFile(labeledFile.getAbsolutePath),
        entityTpes.toUpperCase(), ",", ner.transformTextFromFile(labeledFile.getAbsolutePath))


//      println("\n\nEvaluation result for company " + company)
      res  :+= List(company, f"${ner.precision}%.3f", f"${ner.recall}%.3f", f"${ner.f1}%.3f")
      tp += ner.tp
      fp += ner.fp
      fn += ner.fn
      tn += ner.tn
    }
    val ner = new NerEvaluation(tp, fp, fn, tn)
    res :+= List("Overall", f"${ner.precision}%.3f",
      f"${ner.recall}%.3f",
      f"${ner.f1}%.3f")

    println()
    println(Tabulator.format(res))
  }

}
