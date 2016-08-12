package com.intel.ie.evaluation

import java.io.FileInputStream
import java.util.Properties

import com.intel.ie.evaluation.preparation.crawl.ProxyConfig

import scala.io.Source
import scala.reflect.io.File

/**
  * Created by xianyan on 8/2/16.
  */
object EvalPaths {
  def rawPage(company: String) = s"data/evaluation/raw/raw-${company}.txt"

  var urlsPath: String = "data/evaluation/page-urls.txt"
//  var webContentPath: String = "data/evaluation/web/%s/page-%s.txt"
  //  var labeledPath: String =
  //  var extractionPath: String =


  def webContentPath(label: String, i: Int): String = {
    val root = "data/evaluation/web/"
    val dir = File(s"${root}${label}/")
    if(!dir.exists) dir.createDirectory()
    s"${root}$label/page-${label}_${i}.txt"
  }

  def labeledPath(company: String) = s"data/evaluation/labeled/labeled-${company}.txt"

  def extractionPath(company: String) = s"data/evaluation/extraction/extraction-${company}.csv"

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
