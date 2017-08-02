package haishu.crawler2.selector

import java.util.regex.{Pattern, PatternSyntaxException}

import org.apache.commons.lang3.StringUtils

class RegexSelector(pattern: Pattern, group: Int = 1) extends Selector {

  override def select(text: String): String = selectGroup(text).get(group)

  override def selectSeq(text: String): Seq[String] = {
    selectGroupSeq(text).map(_.get(group))
  }

  def selectGroup(text: String): RegexResult = {
    val matcher = pattern.matcher(text)
    if (matcher.find()) {
      val groups = Array.tabulate(matcher.groupCount() + 1)(matcher.group)
      RegexResult(groups)
    } else RegexResult.empty
  }

  def selectGroupSeq(text: String): Seq[RegexResult] = {
    val matcher = pattern.matcher(text)
    var resultSeq: Seq[RegexResult] = Vector()
    while (matcher.find()) {
      val groups = Array.tabulate(matcher.groupCount() + 1)(matcher.group)
      resultSeq :+= RegexResult(groups)
    }
    resultSeq
  }
}

object RegexSelector {

  def apply(expr: String, group: Int): RegexSelector = new RegexSelector(compileRegex(expr), group)

  def apply(expr: String): RegexSelector = {
    val p = compileRegex(expr)
    val group = if (p.matcher("").groupCount() == 0) 0 else 1
    new RegexSelector(p, group)
  }

  private def compileRegex(expr: String): Pattern = {
    if (StringUtils.isBlank(expr)) throw new IllegalArgumentException("regex must not be empty")

    try {
      Pattern.compile(expr, Pattern.DOTALL | Pattern.CASE_INSENSITIVE)
    } catch {
      case e: PatternSyntaxException =>
        throw new IllegalArgumentException("invalid regex " + expr, e)
    }
  }

}
