import java.nio.charset.Charset

case class HtmlResponse(
    status: Int,
    headers: Map[String, Seq[String]],
    body: Array[Byte],
    request: Request,
    encoding: Option[Charset] = None) extends Response {

  private lazy val text = new String(body, encoding.getOrElse(Charset.defaultCharset()))

  private lazy val html = Html(text)

}