package evaluation.preparation

/**
  * Created by xianyan on 7/28/16.
  */
object CleanTest {

  def main(args: Array[String]): Unit = {
    val proxy = CrawlerHelper.getProxy()
    val crawler = new Crawler(proxy)
    while (true) {
      val urlMap = CrawlerHelper.getUrlMap()
      for ((label, urlStr) <- urlMap) {
        val urls = urlStr.split("\t")
        var i = 0;
        for (url <- urls) {
          crawler.crawlAndSave(url, label, i)
          i += 1
        }
      }
    }

  }

}
