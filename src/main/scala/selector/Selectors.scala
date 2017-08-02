package haishu.crawler2.selector

/**
 * Created by hldev on 7/21/17.
 */
object Selectors {

  def regex(expr: String) = RegexSelector(expr)

  def regex(expr: String, group: Int) = RegexSelector(expr, group)

  def css(expr: String) = CssSelector(expr)

  def css(expr: String, attrName: String) = CssSelector(expr, attrName)

}
