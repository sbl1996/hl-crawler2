package haishu.crawler2.selector

import java.nio.charset.Charset

import org.jsoup.helper.StringUtil
import org.jsoup.nodes.Element

import scala.collection.JavaConverters._
/**
 * Created by hldev on 7/21/17.
 */
class LinksSelector extends BaseElementSelector {

  override def select(element: Element): String =
    throw new UnsupportedOperationException

  override def selectSeq(element: Element): Seq[String] = {
    val elements = element.select("a")
    elements.asScala.map { elem =>
      if (StringUtil.isBlank(elem.baseUri())) {
        elem.attr("abs:href")
      }
      else {
        elem.attr("href")
      }
    }
  }

  override def selectElement(element: Element): Element =
    throw new UnsupportedOperationException

  override def selectElements(element: Element): Seq[Element] =
    throw new UnsupportedOperationException

  override def hasAttribute = true
}

object LinksSelector {
  def apply(): LinksSelector = new LinksSelector()
}
