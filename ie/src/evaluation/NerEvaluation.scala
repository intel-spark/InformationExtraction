package evaluation

import scala.io.Source

/**
  * Created by xianyan on 16-7-27.
  */

class NerEvaluation {
  var tp = 0
  var fp = 0
  var fn = 0
  var tn = 0
  var precision = 0.0
  var recall = 0.0
  var f1 = 0.0

  /**
    * evaluates the precision, recall and f1
    *
    * @param outputs List of output labels of one sentence
    * @param truths  List of true labels of one sentence
    * @param labels  list of labels to be checked
    */
  def eval(outputs: List[String], truths: List[String], labels: List[String], original: String): Unit = {
    println(original)
    print("output: ");
    outputs.foreach(printf("%15s", _));
    println()
    print("truths: ");
    truths.foreach(printf("%15s", _));
    println()

    for ((o, t) <- (outputs zip truths)) {
      if (t == "o") {
        //truth negative
        if (o == "o") tn += 1 else fp += 1
      }
      else if (labels.contains(t)) {
        //truth positive
        if (o == t) tp += 1 else fn += 1
      }
    }
  }


  /**
    *
    * @param outputStr
    * @param truthStr
    * @param labelStr
    * @param splitSign
    */
  def eval(outputStr: String, truthStr: String, labelStr: String, splitSign: String, original: String): Unit = {
    eval(outputStr.split(splitSign).map(_.trim).map(_.toLowerCase).toList,
      truthStr.split(splitSign).map(_.trim).map(_.toLowerCase).toList,
      labelStr.split(splitSign).map(_.trim).map(_.toLowerCase).toList,
      original)
  }

  /**
    *
    * @param outputs       a list of outputs from model
    * @param truths        a list of truths
    * @param labelStr      the labels you want to evaluate,
    *                 e.g. "PERSON" or "PERSON, TITLE" or "PERSON, TITLE, ORGANIZATION", etc
    * @param delimiter     delimiter to split the sentence, "," by default
    * @param originalLines original raw sentences
    */
  def eval(outputs: List[String], truths: List[String], labelStr: String, delimiter: String, originalLines: List[String]): Unit = {
    for (((oLine, tLine), original) <- (outputs zip truths zip originalLines)) {
      eval(oLine, tLine, labelStr, delimiter, original)
    }
    precision = tp * 1.0 / (tp + fp)
    recall = tp * 1.0 / (tp + fn)
    f1 = 2 * (recall * precision) / (recall + precision)
  }

  /**
    * read the labeled truths from the file
    *
    * @param file file path
    * @return a list of labels correspond to each line
    */
  def transformFromFile(file: String): List[String] = {
    transform(Source.fromFile(file).getLines.toList)
  }

  /**
    * extract the label from the string list
    *
    * @param labeled a list of sentence labeled
    * @return a list of labels
    */
  private def transform(labeled: List[String]): List[String] =
    for(line <- labeled) yield transform(line)

  /**
    * extract labels from the sentence
    *
    * @param line one line with label for each word
    * @return label string separated by ","
    */
  private def transform(line: String): String = {
    val items = line.split("\t")
    val tranStr = for(item <- items) yield item.split("/")(1)
    tranStr.mkString(", ")
  }
}