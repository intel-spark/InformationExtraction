package intel.analytics

/**
  * Created by yuhao on 8/2/16.
  */
object IOUtils {
  import scala.Console._

  /** Print and flush formatted text to the default output, and read a full line from the default input.
    *  Returns `null` if the end of the input stream has been reached.
    *
    *  @param text the format of the text to print out, as in `printf`.
    *  @param args the parameters used to instantiate the format, as in `printf`.
    *  @return the string read from the default input
    */
  def readLine(text: String, args: Any*): String = {
    printf(text, args: _*)
    out.flush()
    in.readLine()
  }

  def readLine(): String = in.readLine()

}
