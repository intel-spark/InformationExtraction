package com.intel.ie.evaluation.preparation.crawl

/**
  * Created by xianyan on 16-7-25.
  */

import java.io._
import java.net.{MalformedURLException, URL}

import com.intel.ie.evaluation.EvalPaths
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import scala.collection.JavaConversions._
import scala.util.control.Exception._


class Crawler() {
  val proxy = EvalPaths.proxy()
  if (proxy != null) {
    System.setProperty("http.proxyHost", proxy.host)
    System.setProperty("http.proxyPort", proxy.port)
    System.setProperty("https.proxyHost", proxy.host)
    System.setProperty("https.proxyPort", proxy.port)
  }

  type JDoc = org.jsoup.nodes.Document

  def get(url: String, label: String): JDoc = {
    var newUrl = ""
    if (url.startsWith("http://"))
      newUrl = url;

    if (url.startsWith("www"))
      newUrl = "http://" + url;
    val file = new File(EvalPaths.rawPage(label))

    if (!file.exists()) {
      val doc = Jsoup.connect(newUrl)
        .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
        .timeout(30000)
        .get()
      val writer = new BufferedWriter(new FileWriter(file))
      writer.write(doc.toString)
      writer.close()
      return doc
    } else {
      return Jsoup.parse(file, "UTF-8")
    }

  }


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


  def safeURL(url: String): Option[String] = {
    val result = catching(classOf[MalformedURLException]) opt new URL(url)
    result match {
      case Some(v) => Some(v.toString)
      case None => None
    }
  }


  def transformElement(element: Element): String = {
    element.text() match {
      case "<br>" => ""
      case _ => element.text()
    }
  }

  def extract(doc: JDoc): List[String] = {

    var lastParentStr = ""
    var lastParentUpdated = ""
    var lines = List[String]()

    def extractElement(element: Element): Unit = {
      if (!element.isBlock) {
        if (!element.text().trim.isEmpty) {
          val parentStr = extractParent(element).text()
          if (lastParentStr != parentStr) {
            lines :+= lastParentUpdated
            lastParentStr = parentStr
            lastParentUpdated = lastParentStr
          }
          lastParentUpdated = lastParentUpdated.replace(element.text(), element.text() + ", ")
        }
      }
      else
        for (child <- element.children()) {
          extractElement(child)
        }
    }

    extractElement(doc.body())
    lines
  }


  def isElementTooBroad(parent: Element): Boolean = {
    parent.getAllElements.size() > 20
  }


  def extractParent(element: Element): Element = {
    var parent = element.parent()
    while (parent.text() == element.text()) {
      parent = parent.parent()
    }
    if (isElementTooBroad(parent)) {
      println(s"${parent.text()} is filtered due to too broad")
      return element
    }
    parent
  }

  def save2File(path: File, doc: JDoc, uRL: String) = {
    val bw = new BufferedWriter(new FileWriter(path))
    bw.write(Cleaner.clean(bodyText(doc)))
    bw.newLine()
    bw.write(uRL)
    bw.close()
  }

  def save2File2(saveFile: File, strings: List[String], url: String) = {
    val bw = new BufferedWriter(new FileWriter(saveFile))
    bw.write(Cleaner.clean(strings.mkString("\n")))
    bw.write("\n" + url)
    bw.close()
  }

  def crawlAndSave(url: String, label: String, i: Int): Boolean = {
    try {
      val saveFile = new File(EvalPaths.webContentPath(label, i))
      if (!saveFile.exists()) {
        val doc = Jsoup.parse(get(url, label).html().replaceAll("<br>", ","))
        save2File2(saveFile, extract(doc), url)
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


