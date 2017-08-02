package haishu.crawler2.selector

import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import scala.collection.JavaConverters._

/**
 * Created by hldev on 7/21/17.
 */
class CssSelector(selectorText: String, attrName: String) extends BaseElementSelector {

  private def getValue(element: Element): String = {
    if (attrName == null) element.outerHtml
    else if ("innerHtml".equalsIgnoreCase(attrName)) element.html
    else if ("text".equalsIgnoreCase(attrName)) getText(element)
    else if ("allText".equalsIgnoreCase(attrName)) element.text
    else element.attr(attrName)
  }

  protected def getText(element: Element): String = {
    element.childNodes.asScala.map {
      case node: TextNode => node.text()
      case _              => ""
    }.mkString
  }

  override def select(element: Element): String = {
    val elements = selectElements(element)
    if (elements.isEmpty) null
    else getValue(elements.head)
  }

  override def selectSeq(doc: Element): Seq[String] = {
    val elements = selectElements(doc)
    if (elements.isEmpty) Seq()
    else elements.map(getValue).filter(_ != null)
  }

  override def selectElement(element: Element): Element = {
    val elements = selectElements(element)
    if (elements.isEmpty) null else elements.head
  }

  override def selectElements(element: Element): Seq[Element] = {
    element.select(selectorText).asScala
  }

  override def hasAttribute: Boolean = attrName != null
}

object CssSelector {
  def apply(selectorText: String, attrName: String): CssSelector = new CssSelector(selectorText, attrName)

  def apply(selectorText: String): CssSelector = new CssSelector(selectorText, null)
}
