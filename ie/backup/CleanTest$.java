package com.intel.ie.evaluation.preparation.crawl;

/**
  * Created by xianyan on 7/28/16.
  */
object CleanTest {

  def webContentPath(label: String, i: Int): String = {
    val dir = File(s"${IntelConfig.COMPANY_PAGE_PATH}${label}/")
    if (!dir.exists) dir.createDirectory()
    s"${dir}page-${label}_${i}.txt"
  }

  def webSavePath(label: String, i: Int): String = {
    val dir = File(s"${IntelConfig.COMPANY_PAGE_PATH}${label}/")
    if (!dir.exists) dir.createDirectory()
    s"${dir}page-${label}_${i}.txt"
  }

  def reviseLine(line: String): String =
    line.replaceAll("^(\\s*,\\s*)+", "").replaceAll("(\\s*,\\s*)+", ", ")


  def main(args: Array[String]): Unit = {
    val urlMap = EvalPaths.urlMap()
    for ((label, urlStr) <- urlMap) {
      println(webContentPath(label, 0))
      if (File(webContentPath(label, 0)).exists) {
        val newtext = Source.fromFile(webContentPath(label, 0)).getLines().map(line => {
          reviseLine(line)
        }).mkString("\n")

        val writer = new BufferedWriter(new FileWriter(webSavePath(label, 0)))
        println(newtext)
        writer.write(newtext)
        writer.close()
      }

    }

  }

}
