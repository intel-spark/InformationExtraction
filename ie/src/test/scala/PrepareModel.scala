package Test
import scala.util.control.Breaks._
import java.io._
import java.util

import scala.sys.process._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.io.Source
/**
  * Created by ding on 8/5/16.
  */
//it's better to clean /home/ding/proj/test
//put tag file under /home/ding/proj/InformationExtraction/data/evaluation/manual/tag_test
//put ori file under /home/ding/proj/InformationExtraction/data/evaluation/manual/ori_test
//then run tha app
object PrepareModel {
  def main(args: Array[String]) {

    //get all files with *_final.tsv
    val p = "/home/ding/proj/test"
    val s = new StringBuilder()
    new File(p).listFiles().filter(f => f.getName.endsWith("_final.tsv")).foreach{ x =>
      s.append(x.getName)
      s.append(",")
    }

    println(s)

    //get all files with *.tsv, from *.txt to *.tsv
    val path = "/home/ding/proj/InformationExtraction/ie/data/evaluation/ori_test"
//    val path = "/home/ding/proj/InformationExtraction/data/evaluation/manual/test"
var fileNumber = 0
    new File(path).listFiles().foreach { folder =>
      val company = folder.getName
      folder.listFiles().foreach { file =>

        val test = new File("/home/ding/proj/test/0.txt")
        file.renameTo(test)

        val cmd = "sh /home/ding/proj/test.sh " + test.getParent
        val process = Runtime.getRuntime().exec(cmd)
        val status = process.waitFor();
        if (status != 0) {
          System.err.println("Failed to call shell's command and the return status's is: " + status);
        }

        test.renameTo(file)

        val nameFirstWordHashset = new mutable.HashSet[String]()
        val departmentFirstWordHashset = new mutable.HashSet[String]()
        val titleFirstWordHashset = new mutable.HashSet[String]()
        val nameData = new Array[mutable.HashSet[String]](10)
        val depData = new Array[mutable.HashSet[String]](20)
        val titleData = new Array[mutable.HashSet[String]](20)
        for (i <- 0 until nameData.length) {
          nameData(i) = new mutable.HashSet[String]()
        }
        for (i <- 0 until depData.length) {
          depData(i) = new mutable.HashSet[String]()
        }
        for (i <- 0 until titleData.length) {
          titleData(i) = new mutable.HashSet[String]()
        }

        val tagPath = file.getParentFile.getParentFile.getParent + "/tag_test/" + file.getParent.split("/").last + "/" + file.getName
        if (new File(tagPath).exists()) {

println("file name: "+file.getName)
          Source.fromFile(tagPath).getLines.toList.foreach { line =>
            if (line.contains("\t1")) {
              val nameStart = line.indexOf("\t1") + 2
              var nameEnd = line.indexOf("\t", nameStart)
              if(nameEnd == -1) nameEnd = line.length
              val name = line.substring(nameStart, nameEnd)
              nameFirstWordHashset.add(name.split(" ").apply(0))
//              println("name:"+name)
              nameData(name.split(" ").length - 1).add(name)


              var di = 0
              while (line.indexOf("\t3", di) != -1) {
                val departStart = line.indexOf("\t3", di) + 2
                var departEnd = line.indexOf("\t", departStart)
                if (departEnd == -1) departEnd = line.length
                if (departEnd <= departStart) {
                  throw new RuntimeException(s"departments error: $company, $line")
                }
                val department = (line.substring(departStart, departEnd))
                departmentFirstWordHashset.add(department.split(" ").apply(0))
                depData(department.split(" ").length - 1).add(department)
                di = departEnd
              }

              var ti = 0
              while (line.indexOf("\t2", ti) != -1) {
                val titleStart = line.indexOf("\t2", ti) + 2
                var titleEnd = line.indexOf("\t", titleStart)
                if (titleEnd == -1) titleEnd = line.length
                if (titleEnd <= titleStart) {
                  throw new RuntimeException(s"departments error: $company, $line")
                }
                val title = (line.substring(titleStart, titleEnd))
                titleFirstWordHashset.add(title.split(" ").apply(0))
//                println("title " + title)
                titleData(title.split(" ").length - 1).add(title)
                ti = titleEnd
              }
            }
          }
//          println("name first letter")
//          nameFirstWordHashset.foreach(println(_))
//          println("name")
//          nameData.filter(p => p.size > 0).foreach(println(_))

//          println("dep first letter")
//          departmentFirstWordHashset.foreach(println(_))
//          println("dep")
//          depData.filter(p => p.size > 0).foreach(println(_))

//            println("title first letter")
//            titleFirstWordHashset.foreach(println(_))
//            println("title")
//            titleData.filter(p => p.size > 0).foreach(println(_))

          val lastName2 = file.getName.replace(" ","_")
          val lastName = lastName2.substring(0, lastName2.length-4)
          val file1Name = "/home/ding/proj/test/" +lastName + "_person.txt"
          val file2Name = "/home/ding/proj/test/"+lastName +"_dep.txt"
          val file3Name = "/home/ding/proj/test/"+lastName +"_title.txt"
          person(nameFirstWordHashset, nameData, file1Name )
          department(departmentFirstWordHashset, depData, file2Name)
          title(titleFirstWordHashset, titleData, file3Name)
          merge(file1Name, file2Name, file3Name, lastName)
        }
        fileNumber += 1
        println("Finished file " + fileNumber)
      }
    }
  }

  def person(nameFirstWordHashset: mutable.HashSet[String], nameData: Array[mutable.HashSet[String]], lastName: String) = {
    val f2 = new File(lastName) // Temporary File
    val w = new PrintWriter(f2)
    var tmpName = new ArrayBuffer[String]()
    var readNextName = false
    val lines = Source.fromFile("/home/ding/proj/test/file.tsv").getLines.toList
    var i = 0
    while(i < lines.length) {
      val line = lines(i)
      val str = line.split("\t").head

      var nameStart = 0
      var nameEnd = -1
      if(nameFirstWordHashset.contains(str)) {
        tmpName = new ArrayBuffer[String]()
        val iBackup = i
        nameStart = i

        val j = math.min(lines.length, nameData.length+i)
        while(i < j) {
          tmpName += lines(i).split("\t").head
          i += 1
        }
        var k = j-iBackup-1
        while(k>=0) {

          if(nameData(k).contains(tmpName.mkString(" "))) {
            nameEnd = iBackup+k
            k = -1
          }
          k -= 1
          tmpName.remove(tmpName.length-1)
        }
        i = iBackup
      }
      if(nameEnd >= nameStart) {
        var m = nameStart
        while(m <= nameEnd) {
          val str2 = lines(m).split("\t").head
          w.println(str2 + "\t" +"PERSON")
          m += 1
        }
        i = nameEnd + 1
      } else {
        w.println(line)
        i+=1
      }
    }
    w.close()
  }

  def department(depFirstWordHashset: mutable.HashSet[String], depData: Array[mutable.HashSet[String]], lastName: String) = {
    val f2 = new File(lastName) // Temporary File
    val w = new PrintWriter(f2)
    var tmpName = new ArrayBuffer[String]()
    var readNextName = false
    val lines = Source.fromFile("/home/ding/proj/test/file.tsv").getLines.toList
    var i = 0
    while(i < lines.length) {
      val line = lines(i)
      val str = line.split("\t").head

      var depStart = 0
      var depEnd = -1
      if(depFirstWordHashset.contains(str)) {
        tmpName = new ArrayBuffer[String]()
        val iBackup = i
        depStart = i

        val j = math.min(lines.length, depData.length+i)
        while(i < j) {
          tmpName += lines(i).split("\t").head
          i += 1
        }
        var k = j-iBackup-1
        while(k>=0) {

          if(depData(k).contains(tmpName.mkString(" "))) {
            depEnd = iBackup+k
            k = -1
          }
          k -= 1
          tmpName.remove(tmpName.length-1)
        }
        i = iBackup
      }
      if(depEnd >= depStart) {
        var m = depStart
        while(m <= depEnd) {
          val str2 = lines(m).split("\t").head
          w.println(str2 + "\t" +"DEPARTMENT")
          m += 1
        }
        i = depEnd + 1
      } else {
        w.println(line)
        i+=1
      }
    }
    w.close()
  }

  def title(titleFirstWordHashset: mutable.HashSet[String], titleData: Array[mutable.HashSet[String]], lastName: String) = {
    val f2 = new File(lastName) // Temporary File
    val w = new PrintWriter(f2)
    var tmpName = new ArrayBuffer[String]()
    var readNextName = false
    val lines = Source.fromFile("/home/ding/proj/test/file.tsv").getLines.toList
    var i = 0
    while(i < lines.length) {
      val line = lines(i)
      val str = line.split("\t").head

      var depStart = 0
      var depEnd = -1
      if(titleFirstWordHashset.contains(str)) {
        tmpName = new ArrayBuffer[String]()
        val iBackup = i
        depStart = i

        val j = math.min(lines.length, titleData.length+i)
        while(i < j) {
          tmpName += lines(i).split("\t").head
          i += 1
        }
        var k = j-iBackup-1
        while(k>=0) {

          if(titleData(k).contains(tmpName.mkString(" "))) {
            depEnd = iBackup+k
            k = -1
          }
          k -= 1
          tmpName.remove(tmpName.length-1)
        }
        i = iBackup
      }
      if(depEnd >= depStart) {
        var m = depStart
        while(m <= depEnd) {
          val str2 = lines(m).split("\t").head
          w.println(str2 + "\t" +"TITLE")
          m += 1
        }
        i = depEnd + 1
      } else {
        w.println(line)
        i+=1
      }
    }
    w.close()
  }

  def merge(file1Name: String, file2Name: String, file3Name: String, name: String)={
    var i = 0
    val personLines = Source.fromFile(file1Name).getLines.toList
    val depLines = Source.fromFile(file2Name).getLines.toList
    val titleLines = Source.fromFile(file3Name).getLines.toList
    val f2 = new File("/home/ding/proj/test/"+name+"_final.tsv") // Temporary File
    val w = new PrintWriter(f2)
    while(i < personLines.length) {
      if(personLines(i).split("\t").last!="O") {
        w.println(personLines(i))
      } else if(titleLines(i).split("\t").last!="O") {
        w.println(titleLines(i))
      } else {
        w.println(depLines(i))
      }
      i += 1
    }
    w.close()
  }
}
