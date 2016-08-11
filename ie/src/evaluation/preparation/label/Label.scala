package evaluation.preparation.label

import java.io.{BufferedWriter, FileWriter}

import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation
import edu.stanford.nlp.pipeline.Annotation
import edu.stanford.nlp.util.CoreMap
import evaluation.EvalPaths
import evaluation.preparation.NerHelper
import intel.analytics.IOUtils

import scala.collection.mutable.ListBuffer
import scala.io.Source

/**
  * Created by xianyan on 16-7-27.
  */
object Label {


  val titleList = List {}
  val person = "PERSON"
  val title = "TITLE"
  val organization = "ORGANIZATION"


  def main(args: Array[String]): Unit = {
    val urlMap = EvalPaths.urlMap()
    val company = "AECOM"
    val result = labelCompany(company, 0)
    val bw = new BufferedWriter(new FileWriter(EvalPaths.labeledPath(company)))
    bw.write(result)
    bw.close()
    Extraction.extract(company)
  }

  class FixedList[A](max: Int) extends Traversable[A] {

    val list: ListBuffer[A] = ListBuffer()

    def append(elem: A) {
      if (list.size == max) {
        list.trimStart(1)
      }
      list.append(elem)
    }

    def removeLast(): A = {
      list.remove(list.length - 1)
    }

    def foreach[U](f: A => U) = list.foreach(f)

  }

  def labelCompany(company: String, index: Int): String = {
    val lines = Source.fromFile(EvalPaths.webContentPath(company, index)).getLines().toList
    var labeledLines = ListBuffer[String]()
    for (line <- lines) {
      println(line)
      val document: Annotation = new Annotation(line)
      NerHelper.pipeline.annotate(document)
      val sentences: java.util.List[CoreMap] = document.get(classOf[CoreAnnotations.SentencesAnnotation])
      var result = List[String]()
      import scala.collection.JavaConversions._
      for (sentence <- sentences) {
        // traversing the words in the current sentence
        // a CoreLabel is a CoreMap with additional token-specific methods
        import scala.collection.JavaConversions._
        var historyBuffer = new FixedList[String](10)
        var redos = ListBuffer[String]()

        def processOneWord(word: String): Any = {
          println(word)

          var lab = IOUtils.readLine()
          var label = "O"
          lab match {
            case "1" => label = person
            case "2" => label = title
            case "3" => label = organization
            case "c" => {
              redos :+= word
              redos :+= (historyBuffer.removeLast())
            }
            case _ => label
          }
          if (lab == "c") result = result.dropRight(1)
          else result :+= (word + "/" + label)
          if (lab != "c") historyBuffer.append(word)

        }


        for (token <- sentence.get(classOf[CoreAnnotations.TokensAnnotation])) {
          while (!redos.isEmpty) {
            val word = redos.remove(redos.length - 1)
            processOneWord(word)
          }
          // this is the text of the token
          val word = token.get(classOf[TextAnnotation])

          processOneWord(word)
        }
      }
      labeledLines :+= result.mkString("\t")
    }
    labeledLines.mkString("\n")
  }

}
