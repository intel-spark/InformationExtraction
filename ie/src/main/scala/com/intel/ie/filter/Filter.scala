package com.intel.ie.filter

import scala.io.Source

/**
  * Created by xianyan on 8/3/16.
  */
object Filter {
  private val badWords = Source.fromFile("data/filter/badWords.txt").getLines().toSet

  def containBadWord(string: String): Boolean = {
    val words = string.split("\\s+|,|;|\\.")
    for( word <- words ) {
      if (badWords.contains(word)) return true
    }
    false
  }

  def main(args: Array[String]): Unit = {
    println(containBadWord("he is a. bitch."))
  }
}
