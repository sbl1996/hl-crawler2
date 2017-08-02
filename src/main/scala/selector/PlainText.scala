package haishu.crawler2.selector

/**
 * Created by hldev on 7/21/17.
 */
class PlainText(val sourceTexts: Seq[String]) extends AbstractSelectable {

  override def css(selector: String) =
    throw new UnsupportedOperationException("$css can not apply to plain text. Please check whether you use a previous xpath with attribute select (/@href etc).")

  override def css(selector: String, attrName: String) =
    throw new UnsupportedOperationException("$css can not apply to plain text. Please check whether you use a previous xpath with attribute select (/@href etc).")

  override def links() =
    throw new UnsupportedOperationException("Links can not apply to plain text. Please check whether you use a previous xpath with attribute select (/@href etc).")

}

object PlainText {
  def apply(text: String) = new PlainText(Seq(text))

  def apply(sourceTexts: Seq[String]): PlainText = new PlainText(sourceTexts)
}
