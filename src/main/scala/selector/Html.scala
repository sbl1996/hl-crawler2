package haishu.crawler2.selector

import com.typesafe.scalalogging.Logger
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Created by hldev on 7/21/17.
 */
class Html(val document: Document) extends HtmlNode(Seq(document))

object Html {

  lazy val log = Logger("Html")

  def apply(text: String): Html = new Html(Jsoup.parse(text))

  def apply(text: String, url: String): Html = new Html(Jsoup.parse(text, url))

  // TODO: construct failure leads to warn

  /*def apply(text: String): Html = {
    try {
      new Html(Jsoup.parse(text))
    } catch {
      case NonFatal(e) =>
        log.warn("parse document error ", e)
    }
  }

  def apply(text: String, url: String): Html = {
    try {
      new Html(Jsoup.parse(text, url))
    } catch {
      case NonFatal(e) =>
        log.warn("parse document error ", e)
    }
  }*/
}
