package haishu.crawler2.selector

import org.jsoup.nodes.{Document, Element}

/**
 * Created by hldev on 7/21/17.
 */
class HtmlNode(elements: Seq[Element]) extends AbstractSelectable {

  lazy val sourceTexts: Seq[String] = elements.map(_.toString)

  lazy val links: Selectable = selectElements(LinksSelector())

  protected def selectElements(elementSelector: BaseElementSelector): Selectable = {
    if (!elementSelector.hasAttribute) {
      val resultElements = elements.flatMap { element =>
        val documentElement = checkElementAndConvert(element)
        elementSelector.selectElements(documentElement)
      }
      new HtmlNode(resultElements)
    } else {
      val resultStrings = elements.flatMap { element =>
        val documentElement = checkElementAndConvert(element)
        elementSelector.selectSeq(documentElement)
      }
      new PlainText(resultStrings)
    }
  }

  /**
   * Only document can be select
   * See: https://github.com/code4craft/webmagic/issues/113
   */
  private def checkElementAndConvert(element: Element): Element = element match {
    case d: Document => d
    case _ =>
      val root = new Document(element.ownerDocument().baseUri())
      root.appendChild(element.clone())
      root
  }

  override def css(selector: String): Selectable = {
    val cssSelector = Selectors.css(selector)
    selectElements(cssSelector)
  }

  override def css(selector: String, attrName: String): Selectable = {
    val cssSelector = Selectors.css(selector, attrName)
    selectElements(cssSelector)
  }
}
