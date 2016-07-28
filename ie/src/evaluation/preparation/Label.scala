package evaluation.preparation

import java.io.{BufferedWriter, File, FileWriter}
import java.util.Properties

import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.util.CoreMap
import intel.analytics.IntelPaths

import scala.io.{Source, StdIn}

/**
  * Created by xianyan on 16-7-27.
  */
object Label {


  //  val titleList = List("president", "ceo", "officer", "chairman")
  val titleList = List {}
  val person = "PERSON"
  val title = "TITLE"
  val organization = "ORGANIZATION"
  val startPerson = "<PERSON>"
  val endPerson = "</PERSON>"
  val startTitle = "<TITLE>"
  val endTitle = "</TITLE>"
  val startOrganization = "<ORGANIZATION>"
  val endOrganization = "</ORGANIZATION>"
  private val props: Properties = new Properties
  private var pipeline: StanfordCoreNLP = null
  props.put("annotators", "tokenize, ssplit, pos, lemma, ner, regexner")
  val options2 = "ignorecase=true,validpospattern=^(NN|JJ).*," + IntelPaths.Regex_NER_caseless + ";" + IntelPaths.Regex_NER_cased + ";" + IntelPaths.Regex_NER_department_caseless

  props.setProperty("regexner.mapping", options2)
  pipeline = new StanfordCoreNLP(props)

//  def main(args: Array[String]): Unit = {
//    val urlMap = CrawlerHelper.getUrlMap()
//    val label = "Walmart"
//    val result = labelCompany(label, 0)
//    val bw = new BufferedWriter(new FileWriter(CrawlerHelper.getLabeledFile()))
//    bw.write(wd.title + "\n\n" + wd.body)
//    bw.close()
////    for ((label, urlStr) <- urlMap) {
////      val urls = urlStr.split("\t")
////      var i = 0;
////      for (url <- urls) {
////        println(labelCompany(label, i))
////        i += 1
////      }
////    }
//  }

  def main(args: Array[String]): Unit = {
    val urlMap = CrawlerHelper.getUrlMap()
    val label = "Walmart"
    val result = labelCompany2(label, 0)
    val bw = new BufferedWriter(new FileWriter(CrawlerHelper.getLabeledFile(label)))
    bw.write(result)
    bw.close()
    //    for ((label, urlStr) <- urlMap) {
    //      val urls = urlStr.split("\t")
    //      var i = 0;
    //      for (url <- urls) {
    //        println(labelCompany(label, i))
    //        i += 1
    //      }
    //    }
  }

  def labelCompany2(company: String, index: Int): String = {
    val lines = Source.fromFile(CrawlerHelper.getWebContentPath(company, index)).getLines().toList
    var labeledLines = List[String]()
    for (line <- lines) {
      println(line)
      val document: Annotation = new Annotation(line)
      pipeline.annotate(document)
      val sentences: java.util.List[CoreMap] = document.get(classOf[CoreAnnotations.SentencesAnnotation])
      val result: java.util.List[String] = new java.util.ArrayList[String]
      import scala.collection.JavaConversions._
      for (sentence <- sentences) {
        // traversing the words in the current sentence
        // a CoreLabel is a CoreMap with additional token-specific methods
        import scala.collection.JavaConversions._
        for (token <- sentence.get(classOf[CoreAnnotations.TokensAnnotation])) {
          // this is the text of the token
          val word: String = token.get(classOf[CoreAnnotations.TextAnnotation])
          println(word)
          var lab = StdIn.readLine()
          var label = "O"
          lab match {
            case "1" => label = person
            case "2" => label = title
            case "3" => label = organization
            case _ => label
          }
          result.add(word + "/" +label)
        }
      }
      labeledLines :+= result.mkString("\t")
    }
    labeledLines.mkString("\n")
  }

  def labelCompany( company: String, index: Int): String = {
    val lines = Source.fromFile(CrawlerHelper.getWebContentPath(company, index)).getLines().toList
    var content = ""
    for(line <- lines) {
      val items = line.split(",|and")
      if(!items.isEmpty) {
        content += (startPerson+items(0)+ endPerson+" , ")
        content += (startTitle+items(1)+ endTitle+" , ")
        for(item <- items.slice(2, items.length)) {
          if(isTitle(item)) {
            content += startTitle + item + endTitle + " , "
          } else {
            println(item)
            var lab = StdIn.readLine()
            if (lab == "2") content += startTitle + item + endTitle + " , "
            else if (lab == "3") content += startOrganization + item + endOrganization + " , "
            else content += lab + " , "
          }
        }
        content = content.substring(0, content.length-3)
        content += "\n"
      }
    }
    content
  }

  def isTitle(text: String): Boolean = {
    for (title <- titleList) {
      if (text.toLowerCase().contains(title)) {
        return true
      }
    }
    return false
  }


}
