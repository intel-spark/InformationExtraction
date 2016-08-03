package evaluation.preparation

import scala.collection.mutable.ListBuffer

/**
  * Created by xianyan on 8/1/16.
  */
object HistoryBuffer {

  class FixedList[A](max: Int) extends Traversable[A] {

    val list: ListBuffer[A] = ListBuffer()

    def append(elem: A) {
      if (list.size == max) {
        list.trimStart(1)
      }
      list.append(elem)
    }

    def removeLast(): Unit = {
      list.remove(list.length-1)
    }

    def foreach[U](f: A => U) = list.foreach(f)

  }

  def main(args: Array[String]): Unit = {
    var list = List[String]()
    list :+= "test"
    list :+= "ddd"
    println(list)

    println(list.dropRight(1))
  }
}
