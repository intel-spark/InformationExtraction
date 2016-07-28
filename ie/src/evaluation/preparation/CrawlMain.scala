package evaluation.preparation

import java.io._

/**
  * Created by xianyan on 16-7-25.
  */
object CrawlMain {

  def main(args: Array[String]): Unit = {
    val proxy = CrawlerHelper.getProxy()
    val crawler = new Crawler(proxy)
    val urlMap = CrawlerHelper.getUrlMap()
    for((label, urlStr) <- urlMap) {
      val urls = urlStr.split("\t")
      var i = 0;
      for(url <- urls) {
        crawler.crawlAndSave(url, label, i)
        i += 1
      }

    }
  }


}
