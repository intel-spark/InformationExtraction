package evaluation

import scala.io.Source

/**
  * Created by xianyan on 16-7-27.
  */

class NerEvaluation(var tp: Int, var fp: Int, var fn: Int, var tn: Int) {

  def this() {
    this(0, 0, 0, 0)
  }

  //  var precision = 0.0
  //  var recall = 0.0
  //  var f1 = 0.0

  /**
    * evaluates the precision, recall and f1
    *
    * @param outputs List of output labels of one sentence
    * @param truths  List of true labels of one sentence
    * @param labels  list of labels to be checked
    */
  def eval(outputs: List[String], truths: List[String], labels: List[String], original: List[String]): Unit = {
    //    println(original)
    //    print("output: ");
    //    outputs.foreach(printf("%15s", _));
    //    println()
    //    print("truths: ");
    //    truths.foreach(printf("%15s", _));
    //    println()

    for (((o, t), r) <- (outputs zip truths zip original)) {
      if (t == "o") {
        //truth negative
        if (o == "o") tn += 1 else fp += 1
      }
      else if (labels.contains(t)) {
        //truth positive
        if (o == t)
          tp += 1
        else {
          fn += 1
          //todo: not aligned for original sentence
          if(!r.trim.isEmpty)
          println(
            Console.RED + r +
              Console.BLACK + " not recognized as " +
              Console.RED + t +
              Console.BLACK + " in sentence " +
              Console.BLUE + original.mkString(" "))
        }
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
    eval(outputStr.split("\t").map(_.trim).map(_.toLowerCase).toList,
      truthStr.split("\t").map(_.trim).map(_.toLowerCase).toList,
      labelStr.split(splitSign).map(_.trim).map(_.toLowerCase).toList,
      original.split("\t").map(_.trim).toList)
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
    //    precision = tp * 1.0 / (tp + fp)
    //    recall = tp * 1.0 / (tp + fn)
    //    f1 = 2 * (recall * precision) / (recall + precision)
  }

  def precision = tp * 1.0 / (tp + fp)

  def recall = tp * 1.0 / (tp + fn)

  def f1 = 2 * (recall * precision) / (recall + precision)

  //  def precision(tp: Int, fp: Int) =  tp * 1.0 / (tp + fp)
  //  def recall(tp: Int, fn: Int) = tp * 1.0 / (tp + fn)
  //  def f1(precision: Int, recall: Int) = 2 * (recall * precision) / (recall + precision)
  /**
    * read the labeled truths from the file
    *
    * @param file file path
    * @return a list of labels correspond to each line
    */
  def transformLabelFromFile(file: String): List[String] = {
    transformLabel(Source.fromFile(file).getLines.toList)
  }

  /**
    * extract the label from the string list
    *
    * @param labeled a list of sentence labeled
    * @return a list of labels
    */
  private def transformLabel(labeled: List[String]): List[String] =
  for (line <- labeled) yield transformLabel(line)

  /**
    * extract labels from the sentence
    *
    * @param line one line with label for each word
    * @return label string separated by ","
    */
  private def transformLabel(line: String): String = {
    val items = line.split("\t")
    val tranStr = for (item <- items; eles = item.split("/") if (eles.length > 1)) yield eles(1)
    tranStr.mkString("\t")
  }

  def transformTextFromFile(file: String): List[String] = {
    transformText(Source.fromFile(file).getLines.toList)
  }

  /**
    * extract the label from the string list
    *
    * @param labeled a list of sentence labeled
    * @return a list of labels
    */
  private def transformText(labeled: List[String]): List[String] = {
    for (line <- labeled) yield transformText(line)
  }

  /**
    * extract labels from the sentence
    *
    * @param line one line with label for each word
    * @return label string separated by ","
    */
  private def transformText(line: String): String = {
    val items = line.split("\t")
    val tranStr = for (item <- items; eles = item.split("/") if (eles.length > 1)) yield eles(0).trim
    tranStr.mkString("\t")
  }
}