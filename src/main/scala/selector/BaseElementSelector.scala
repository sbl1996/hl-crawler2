package haishu.crawler2.selector

import org.jsoup.Jsoup
import org.jsoup.nodes.Element

/**
 * Created by hldev on 7/21/17.
 */
abstract class BaseElementSelector extends Selector with ElementSelector {

  override def select(text: String): String = {
    if (text != null) select(Jsoup.parse(text)) else null
  }

  override def selectSeq(text: String): Seq[String] = {
    if (text != null) selectSeq(Jsoup.parse(text)) else Seq()
  }

  def selectElement(text: String): Element = {
    if (text != null) selectElement(Jsoup.parse(text)) else null
  }

  def selectElements(text: String): Seq[Element] = {
    if (text != null) selectElements(Jsoup.parse(text)) else Seq()
  }

  def selectElement(element: Element): Element

  def selectElements(element: Element): Seq[Element]

  def hasAttribute: Boolean
}
