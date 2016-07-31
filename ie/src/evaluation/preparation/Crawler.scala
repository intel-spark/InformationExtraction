package evaluation.preparation

/**
  * Created by xianyan on 16-7-25.
  */

import java.io._
import java.net.{MalformedURLException, URL}

import org.jsoup.Jsoup

import scala.collection.JavaConversions._
import scala.util.control.Exception._


sealed case class Link(title: String, href: String)

case class WebDocument(title: String,
                       body: String,
                       links: Seq[Link],
                       metaDescription: String)

class Crawler(proxy: ProxyConfig) {


  if (proxy != null) {
    System.setProperty("http.proxyHost", proxy.host)
    System.setProperty("http.proxyPort", proxy.port)
    System.setProperty("https.proxyHost", proxy.host)
    System.setProperty("https.proxyPort", proxy.port)
  }

  type JDoc = org.jsoup.nodes.Document

  def get(url: String): JDoc = {
    Jsoup.connect(url)
      .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
      .get()
  }

  def titleText(doc: JDoc): String = doc.select("title").text

  def bodyText(doc: JDoc): String = {
    doc.select("body").text

    val elements = doc.body().select("*")
    var text = ""
    for (ele <- elements) {
      if (!ele.ownText().isEmpty) {
        text += ele.ownText() + "\n"
      }

    }
    text
  }

  /**
    * Allows for extraction without null pointer exceptions
    *
    */
  def safeMetaExtract(doc: JDoc, meta: String): String = {
    val result = doc.select("meta[name=" ++ meta ++ "]").first
    Option(result) match {
      case Some(v) => v.attr("content")
      case None => ""
    }
  }

  def metaKeywords(doc: JDoc): String = safeMetaExtract(doc, "keywords")

  def metaDescription(doc: JDoc): String = safeMetaExtract(doc, "description")

  /**
    * Extracts links from a document
    *
    */
  def linkSequence(doc: JDoc): Seq[Link] = {
    val links = doc.select("a[href]").iterator.toList
    links.map { l => Link(l.text, l.attr("href")) }
  }

  def extract(doc: JDoc): WebDocument = {
    val title: String = titleText(doc)
    val body: String = bodyText(doc)
    val links: Seq[Link] = linkSequence(doc)
    val desc: String = metaDescription(doc)

    //    println(title)
    //    println(doc.select("body").text())
    //    println(body)
    return WebDocument(title, body, links, desc)
  }


  def safeURL(url: String): Option[String] = {
    val result = catching(classOf[MalformedURLException]) opt new URL(url)
    result match {
      case Some(v) => Some(v.toString)
      case None => None
    }
  }

  /**
    * Crawl a URL and return a WebDocument
    *
    */
  def crawl(url: String): WebDocument = {
    val f = extract _ compose get
    f(url)
  }

  def save2File(path: File, wd: WebDocument) = {
    val bw = new BufferedWriter(new FileWriter(path))
    bw.write(wd.title + "\n\n" + Cleaner.clean(wd.body))
    bw.close()
  }

  def crawlAndSave(url: String, label: String, i: Int): Boolean = {
    try {
      val saveFile = new File(CrawlerHelper.getWebContentPath(label, i))
      if (!saveFile.exists()) {
        val wd = crawl(url)
        save2File(saveFile, wd)
        println(url + " done")
        return true
      }
    } catch {

      case ex: Exception => {
        println("error with " + url)
        println(ex.toString)
      }
    }
    return false
  }


}


