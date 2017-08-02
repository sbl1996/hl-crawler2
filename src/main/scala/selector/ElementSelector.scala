package haishu.crawler2.selector

import org.jsoup.nodes.Element

/**
 * Created by hldev on 7/21/17.
 */
trait ElementSelector {

  def select(element: Element): String

  def selectSeq(element: Element): Seq[String]

}
