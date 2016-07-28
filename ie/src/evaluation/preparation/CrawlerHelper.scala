package evaluation.preparation

import java.io.FileInputStream
import java.util.Properties

import scala.io.Source

/**
  * Created by xianyan on 7/27/16.
  */
object CrawlerHelper {
  def getLabeledFile(company: String): String = prop.getProperty("labeled-path").format(company)

  val prop = new Properties()
  prop.load(new FileInputStream("config.properties"))

  def getProxy() = new ProxyConfig(prop.getProperty("proxy-url"), prop.getProperty("proxy-port"), null, null)


  def getWebContentPath(label: String, i: Int): String = {
    prop.getProperty("web-content-path").format(label + "_" + i)
  }

  /**
    *
    * @return a map that map url label to url
    */
  def getUrlMap() : Map[String, String] = Source.fromFile(prop.getProperty("urls-path")).getLines.map(line => {
    val split = line.split("\t", 2)
    split(0) -> split(1)
  }).toMap

}
