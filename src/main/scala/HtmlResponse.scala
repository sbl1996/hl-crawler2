package haishu.crawler2

import java.nio.charset.Charset

import haishu.crawler2.selector.{Html, Selectable}

case class HtmlResponse(
    status: Int,
    headers: Map[String, Seq[String]],
    body: Array[Byte],
    request: Request,
    encoding: Option[Charset] = None) extends Response {

  private lazy val text = new String(body, encoding.getOrElse(Charset.defaultCharset()))

  private lazy val html = Html(text)

  override def css(query: String): Selectable = html.css(query)

  override def css(query: String, attrName: String): Selectable = html.css(query, attrName)

  override def regex(expr: String): Selectable = html.regex(expr)

  override def regex(expr: String, group: Int): Selectable = html.regex(expr, group)

}