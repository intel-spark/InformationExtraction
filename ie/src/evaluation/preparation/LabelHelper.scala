package evaluation.preparation

import intel.analytics.IOUtils

/**
  * usage: copy the relevant lines to console, applicable when there is a "," after person name
  * Created by xianyan on 8/5/16.
  */
object LabelHelper {

  val titles = List(
    "Chairman of the Board",
    "\\w+ Vice President",
    "Chief.*Officer?",
    "CEO",
    "General Counsel.*Secretary?",
    //    "General Counsel",
    "Corporate Controller",
    "Vice President",
    "President",
    "Co-Founder",
    "Treasurer",
    "Corporate Controller",
    "SVP",
    "VP"
  )

  def main(args: Array[String]): Unit = {

    var lastLine = ""
    Iterator.continually(IOUtils.readLine("")).foreach { line =>
      if(lastLine == "" || lastLine.length>10 && line.length > 10 && lastLine.substring(0,10) != line.substring(0,10)) {
        var newLine = ""
        if (!line.startsWith("\t1")) {
          newLine = "\t1" + line
          newLine = newLine.replaceFirst(",", "\t,")
        } else {
          newLine = line
          newLine = newLine.replaceFirst(",", "\t,")
        }
        for (tp <- titles) {
          newLine = ("(" + tp + ")[^\\t]").r.replaceFirstIn(newLine, "\t2$1\t")
        }

        println(newLine)
      } else {
        println(line)
      }
      lastLine = line

    }
  }

}
