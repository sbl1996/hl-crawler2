package haishu.crawler2.selector

/**
 * Created by hldev on 7/21/17.
 */
abstract class AbstractSelectable extends Selectable {

  protected def sourceTexts(): Seq[String]

  protected def select(selector: Selector, strings: Seq[String]): Selectable = {
    val results = strings.map(selector.select).filter(_ != null)
    new PlainText(results)
  }

  protected def selectSeq(selector: Selector, strings: Seq[String]): Selectable = {
    val results = strings.flatMap(selector.selectSeq)
    new PlainText(results)
  }

  override def select(selector: Selector): Selectable = select(selector, sourceTexts())

  override def selectSeq(selector: Selector): Selectable = selectSeq(selector, sourceTexts())

  override def regex(regex: String): Selectable = {
    val regexSelector = Selectors.regex(regex)
    selectSeq(regexSelector, sourceTexts())
  }

  override def regex(regex: String, group: Int): Selectable = {
    val regexSelector = Selectors.regex(regex, group)
    selectSeq(regexSelector, sourceTexts())
  }

  override def replace(regex: String, replacement: String): Selectable = {
    val replaceSelector = ReplaceSelector(regex, replacement)
    select(replaceSelector, sourceTexts())
  }

  override def get(): Option[String] = all().headOption

  def firstSourceText: Option[String] = sourceTexts().headOption

  override def all(): Seq[String] = sourceTexts()

  override def isMatch: Boolean = sourceTexts().nonEmpty
}
