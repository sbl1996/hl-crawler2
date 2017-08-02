package haishu.crawler2.selector

import java.util.regex.{Pattern, PatternSyntaxException}

class ReplaceSelector(private val pattern: Pattern, private val replacement: String) extends Selector {

  override def select(text: String): String = {
    pattern.matcher(text).replaceAll(replacement)
  }

  override def selectSeq(text: String): Seq[String] = throw new UnsupportedOperationException

  override def toString: String = s"${pattern.toString}_$replacement"

}

object ReplaceSelector {

  def apply(regexStr: String, replacement: String): ReplaceSelector = {
    try {
      val p = Pattern.compile(regexStr)
      new ReplaceSelector(p, replacement)
    } catch {
      case e: PatternSyntaxException =>
        throw new IllegalArgumentException("invalid regex", e)
    }
  }

}
