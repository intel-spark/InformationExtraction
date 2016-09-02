package com.intel.ie.evaluation.preparation.crawl

import com.intel.ie.evaluation.EvalPaths

/**
  * Created by xianyan on 16-7-25.
  */
object CrawlMain {

  def main(args: Array[String]): Unit = {
    val urlMap = EvalPaths.urlMap()
    while (true) {
      for ((label, urlStr) <- urlMap) {
        val urls = urlStr.split("\t")
        var i = 0;
        for (url <- urls) {
          Crawler.crawlAndSave(url, label, i)
          i += 1
        }
      }
    }
  }
}

