package evaluation.preparation.label

import java.io.{File, PrintWriter}

import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ArrayBuffer

/**
  * Created by yuhao on 8/4/16.
  */
object TabConverter {
  def main(args: Array[String]) {
    val conf = new SparkConf().setMaster("local").setAppName("ss")
    val sc = new SparkContext(conf)

    val path = "data/evaluation/manual/"
    new File(path).listFiles().foreach { folder =>
      val company = folder.getName
      folder.listFiles().foreach { file =>

        val relations = sc.textFile(file.getPath).flatMap { line =>
          if (line.contains("\t1")){
            val nameStart = line.indexOf("\t1") + 2
            val nameEnd = line.indexOf("\t", nameStart)
            val name = line.substring(nameStart, nameEnd)

            val titles = new ArrayBuffer[String]()
            var ti = 0
            while( line.indexOf("\t2", ti) != -1) {
              val titleStart = line.indexOf("\t2", ti) + 2
              val titleEnd = line.indexOf("\t", titleStart)
              if ( titleEnd <= titleStart) {
                throw new RuntimeException(s"title error: $company, $line")
              }
              titles.+= (line.substring(titleStart, titleEnd))
              ti = titleEnd
            }

            val departments = new ArrayBuffer[String]()
            var di = 0
            while(line.indexOf("\t3", di) != -1) {
              val departStart = line.indexOf("\t3", di) + 2
              val departEnd = line.indexOf("\t", departStart)
              if ( departEnd <= departStart) {
                throw new RuntimeException(s"departments error: $company, $line")
              }
              departments += (line.substring(departStart, departEnd))
              di = departEnd
            }
            val originText = line.replaceAll("\t1", "").replaceAll("\t2", "").replaceAll("\t3", "").replaceAll("\t", "")

            titles.map( title => s"$name\ttitle\t$title\t$originText")
              .union(departments.map( department => s"$name\temployee_of\t$department\t$originText"))
              .toIterator
          }
          else{
            Array[String]().toIterator
          }
        }

        relations.collect().foreach(println)

        val outputFolder = "data/evaluation/extraction/" + company
        new File(outputFolder).mkdir()
        val pw = new PrintWriter(outputFolder + "/" + file.getName)
        relations.collect().foreach( line => pw.println(line))
        pw.flush()
        pw.close()
      }
    }

  }



}
