package com.intel.ie.dataExtraction

import scala.io.Source


object DirectExtraction {



  def extract(source: String): (String, String, String) = {

    val nameSet = Source.fromFile("data/dict/name.dict").getLines().map(_.trim)
    val titleSet = Source.fromFile("data/dict/title.dict").getLines().map(_.trim)
    val departmentSet = Source.fromFile("data/dict/department.dict").getLines().map(_.trim)

    val name = nameSet.filter(source.contains(_)).toSeq
    val title = titleSet.filter(source.contains(_)).toSeq
    val department = departmentSet.filter(source.contains(_)).toSeq

    if (name.nonEmpty && title.nonEmpty ) {
      (name.head, title.head, if (department.nonEmpty) department.head else "")
    } else {
      null
    }

  }

  def main(args: Array[String]) {
    println(extract("Angela Ahrendts Senior Vice President Retail and Online Stores"))
  }

}
