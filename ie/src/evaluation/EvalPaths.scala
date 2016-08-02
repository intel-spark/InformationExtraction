package evaluation

import java.io.FileInputStream
import java.util.Properties

import evaluation.preparation.crawl.ProxyConfig

import scala.io.Source

/**
  * Created by xianyan on 8/2/16.
  */
object EvalPaths {
  def rawPage(company: String) = "data/evaluation/raw/raw-%s.txt".format(company)

  var urlsPath: String = "data/evaluation/page-urls.txt"
  var webContentPath: String = "data/evaluation/web/page-%s.txt"
//  var labeledPath: String =
//  var extractionPath: String =


  def webContentPath(label: String, i: Int): String = {
    webContentPath.format(label + "_" + i)
  }

  def labeledPath(company: String) = "data/evaluation/labeled/labeled-%s.txt".format(company)

  def extractionPath( company: String) = "data/evaluation/extraction/extraction-%s.csv".format(company)
  /**
    *
    * @return a map that map url label to url
    */
  def urlMap(): Map[String, String] = {
    val lines = Source.fromFile(urlsPath).getLines
    var map = Map[String, String]()
    for (line <- lines) {
      val split = line.split("\t", 2)
      if (split.length > 1) map += split(0) -> split(1)
    }
    return map
  }

  def proxy(): ProxyConfig = {
    val prop = new Properties()
    prop.load(new FileInputStream("proxy.properties"))
    if (!prop.isEmpty) {
      return new ProxyConfig(prop.getProperty("proxy-url"), prop.getProperty("proxy-port"), null, null)
    }
    return null
  }
}
