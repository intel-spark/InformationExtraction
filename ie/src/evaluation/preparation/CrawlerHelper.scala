package evaluation.preparation

import java.io.FileInputStream
import java.util.Properties

import scala.io.Source

/**
  * Created by xianyan on 7/27/16.
  */
object CrawlerHelper {


  val prop = new Properties()
  prop.load(new FileInputStream("config.properties"))

  def getProxy() = new ProxyConfig(prop.getProperty("proxy-url"), prop.getProperty("proxy-port"), null, null)


  def getWebContentPath(label: String, i: Int): String = {
    prop.getProperty("web-content-path").format(label + "_" + i)
  }

  def getLabeledFile(company: String) = prop.getProperty("labeled-path").format(company)

  def getExtractionFile( company: String) = prop.getProperty("extraction-path").format(company)
  /**
    *
    * @return a map that map url label to url
    */
  def getUrlMap(): Map[String, String] = {
    val lines = Source.fromFile(prop.getProperty("urls-path")).getLines
    var map = Map[String, String]()
    for (line <- lines) {
      val split = line.split("\t", 2)
      if (split.length > 1) map += split(0) -> split(1)
    }
    return map
  }


}
