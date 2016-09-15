package com.intel.ie.filter

import com.intel.ie.analytics.IntelConfig

import scala.io.Source

object Filter {
  private val badWords = Source.fromFile(IntelConfig.BAD_WORDS_FILE).getLines().toSet

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
