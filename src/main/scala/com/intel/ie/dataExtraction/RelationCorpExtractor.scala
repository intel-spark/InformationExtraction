package com.intel.ie.dataExtraction

import java.io.{BufferedWriter, File, FileWriter}

import com.intel.ie.analytics.IntelConfig
import com.intel.ie.evaluation.preparation.NerHelper
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation
import edu.stanford.nlp.ling.{CoreAnnotations, CoreLabel}
import edu.stanford.nlp.pipeline.Annotation

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

/**
  * Extract and generate the relation corp for model training
  * Created by xianyan on 8/19/16.
  *
  */
object RelationCorpExtractor {

  def cleanLine(line: String, start: Int, end: Int) = line.substring(0, start - 2) + line.substring(start, end) + line.substring(end + 1)


  def main(args: Array[String]): Unit = {
    val savePath = IntelConfig.INTEL_RELATION_CORP
    var writer = new BufferedWriter(new FileWriter(savePath))
    writer.write("# relation corp for model training\n");
    new File(IntelConfig.MANUAL_LABEL_PATH).listFiles().map(f => f.getName).foreach {
      company =>
        //    val company = "A-Mark Precious Metals"
        val path = IntelConfig.MANUAL_LABEL_PATH + s"${company}/page-${company}_0.txt"

        Source.fromFile(path).getLines().filter(!_.trim.isEmpty).zipWithIndex.foreach { case (originText, i) =>
          var line = originText
          var nameIndexes = List[(Int, Int)]()
          var titleIndexes = List[(Int, Int)]()
          var deptIndexes = List[(Int, Int)]()

          if (line.contains("\t1")) {
            var nameStart = line.indexOf("\t1") + 2
            var nameEnd = line.indexOf("\t", nameStart)


            line = cleanLine(line, nameStart, nameEnd)
            nameStart -= 2
            nameEnd -= 2
            nameIndexes :+= (nameStart, nameEnd)
            val titles = new ArrayBuffer[String]()
            var ti = 0
            while (line.indexOf("\t2", ti) != -1) {
              var titleStart = line.indexOf("\t2", ti) + 2
              var titleEnd = line.indexOf("\t", titleStart)
              if (titleEnd <= titleStart) {
                throw new RuntimeException(s"title error: $company, $line")
              }
              titles.+=(originText.substring(titleStart, titleEnd))
              line = cleanLine(line, titleStart, titleEnd)
              titleStart -= 2
              titleEnd -= 2
              titleIndexes :+= (titleStart, titleEnd)
              ti = titleEnd
            }
            val departments = new ArrayBuffer[String]()
            var di = 0
            while (line.indexOf("\t3", di) != -1) {
              var departStart = line.indexOf("\t3", di) + 2
              var departEnd = line.indexOf("\t", departStart)
              if (departEnd <= departStart) {
                throw new RuntimeException(s"departments error: $company, $line")
              }
              departments += (line.substring(departStart, departEnd))
              line = cleanLine(line, departStart, departEnd)
              departStart -= 2
              departEnd -= 2
              deptIndexes :+= (departStart, departEnd)
              di = departEnd
            }

          }
          val document: Annotation = new Annotation(line)
          NerHelper.pipeline.annotate(document)
          val tokens: java.util.List[CoreLabel] = document.get(classOf[CoreAnnotations.TokensAnnotation])
          var cp = 0
          def getEntityType(word: String): String = {
            nameIndexes.foreach { case (s, e) => {
              if (line.indexOf(word, cp) >= s && line.indexOf(word, cp) < e) return "PERSON"
            }
            }

            titleIndexes.foreach { case (s, e) => {
              if (line.indexOf(word, cp) >= s && line.indexOf(word, cp) < e) return "TITLE"
            }
            }

            deptIndexes.foreach { case (s, e) => {
              if (line.indexOf(word, cp) >= s && line.indexOf(word, cp) < e) return "DEPARTMENT"
            }
            }
            return "O"
          }

          var outWordPos = List[Int]()
          var k = 0
          var lastEntity = ""
          var personPos = List[Int]()
          var titlePos = List[List[Int]]()
          var deptPos = List[List[Int]]()

          def collect = {
            if (lastEntity == "PERSON") personPos = outWordPos
            else if (lastEntity == "TITLE") titlePos :+= outWordPos
            else if (lastEntity == "DEPARTMENT") deptPos :+= outWordPos
            outWordPos = List[Int]()
          }

          //          val tokens = sentence.get(classOf[CoreAnnotations.TokensAnnotation])
          tokens.zipWithIndex.foreach { case (token, index) =>
            val word = token.get(classOf[TextAnnotation])
            cp = line.indexOf(word, cp)
            val entity = getEntityType(word)

            if (lastEntity.isEmpty) lastEntity = entity
            if (entity == "O" || entity != lastEntity) {
              collect
              lastEntity = entity
            }

            if (entity == "O" || entity != lastEntity) {
              collect
              outWordPos = List[Int](index)
              lastEntity = entity
            } else if (entity == lastEntity) {
              outWordPos :+= index
            }
          }
          collect

          def output(entityPos: List[List[Int]], relation: String, ner: String): Unit = {
            entityPos.foreach { tp =>
              //              println(relation)
              writer.write(relation + "\n");
              tokens.zipWithIndex.foreach { case (token, index) =>
                var sub = "-"
                var obj = "-"
                var subner = "-"
                var objner = "-"
                if (personPos.contains(index)) {
                  sub = "SUBJECT"
                  subner = "PERSON"
                } else if (tp.contains(index)) {
                  obj = "OBJECT"
                  objner = ner
                }
                writer.write(List(
                  token.get(classOf[TextAnnotation]), sub, subner, obj, objner, "-", "-", "-", "-"
                ).mkString("\t") + "\n")
              }
              writer.newLine()
            }
          }

          if (!personPos.isEmpty) {
            output(titlePos, "per:title", "TITLE")
            output(deptPos, "per:employee_of", "DEPARTMENT")
          } else {
            writer.write("no_relation\n")
            tokens.foreach { token =>
              writer.write((List(token.get(classOf[TextAnnotation])) ::: List.fill(8)("-")).mkString("\t") + "\n")
            }
            writer.newLine()
          }
        }
    }

    writer.close()
  }

}
