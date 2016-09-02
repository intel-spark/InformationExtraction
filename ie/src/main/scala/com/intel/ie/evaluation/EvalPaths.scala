package com.intel.ie.evaluation

import com.intel.ie.IntelConfig
import com.intel.ie.evaluation.preparation.crawl.ProxyConfig

import scala.io.Source
import scala.reflect.io.File

/**
  * Created by xianyan on 8/2/16.
  */
object EvalPaths {
  def rawPage(company: String) = IntelConfig.RAW_PAGE_PATH + s"raw-${company}.txt"

  var urlsPath: String = IntelConfig.URL_LIST


  def webContentPath(label: String, i: Int): String = {
    val dir = File(s"${IntelConfig.COMPANY_PAGE_PATH}${label}/")
    if (!dir.exists) dir.createDirectory()
    s"${dir}page-${label}_${i}.txt"
  }

  def labeledPath(company: String) = IntelConfig.NER_LABELED_PATH + s"labeled-${company}.txt"

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
    if (IntelConfig.PROXY_URL != "" && IntelConfig.PROXY_PORT != "")
      return new ProxyConfig(IntelConfig.PROXY_URL, IntelConfig.PROXY_PORT, null, null)
    return null
  }

}
