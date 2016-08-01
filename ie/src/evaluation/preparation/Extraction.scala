package evaluation.preparation

import java.io.{BufferedReader, BufferedWriter, FileReader, FileWriter}

import au.com.bytecode.opencsv.CSVWriter

import scala.io.Source

/**
  * extract entities from labeled files
  * Created by xianyan on 8/1/16.
  */
object Extraction {

  def extract( labeledFile: String, saveFile: String): Unit = {
    val lines = Source.fromFile(labeledFile).getLines()
    val writer = new CSVWriter(new BufferedWriter(new FileWriter(saveFile)));
    writer.writeNext(Array(Label.person, Label.title, Label.organization))
    for(line <- lines) {
      val res = extractOneLine(line)
      if( res != null ) writer.writeNext( res )

    }
    writer.close()
  }

  def extractOneLine(line: String): Array[String] = {
      println(line)
      var nameList = List[String]()
      var titleList = List[String]()
      var orgList = List[String]()
      val items = line.split("\t")
      var person = ""
      var title = ""
      var org = ""
      var pre = ""
      for (item <- items) {
        val eles = item.split("/")
        if ( eles.length > 1 ) {
          if (eles(1) != pre) {
            if (person != "") nameList :+= person.trim
            else if (title != "") titleList :+= title.trim
            else if (org != "") orgList :+= org.trim
            person = ""; title = ""; org = ""; pre = eles(1)
          }
          eles(1) match {
            case Label.person => person += eles(0) + " "
            case Label.title => title += eles(0) + " "
            case Label.organization => org += eles(0) + " "
            case _ =>
          }
        }
      }
      if (person != "") nameList :+= person.trim
      else if (title != "") titleList :+= title.trim
      else if (org != "") orgList :+= org.trim
      if(!nameList.isEmpty || !titleList.isEmpty || !orgList.isEmpty)
        Array(nameList.mkString("; ") , titleList.mkString("; ") , orgList.mkString("; "))
      else null
  }

  def extract(company: String): Unit = {
    extract(CrawlerHelper.getLabeledFile(company), CrawlerHelper.getExtractionFile(company))
  }

  def main(args: Array[String]): Unit = {
    extract("Walmart")
  }
}
