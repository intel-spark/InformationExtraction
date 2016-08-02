package evaluation.preparation.crawl

import evaluation.EvalPaths

/**
  * Created by xianyan on 7/28/16.
  */
object CleanTest {

  def main(args: Array[String]): Unit = {

    val crawler = new Crawler()
    while (true) {
      val urlMap = EvalPaths.urlMap()
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
