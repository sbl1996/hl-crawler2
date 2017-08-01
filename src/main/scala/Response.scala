import java.nio.charset.Charset
import UrlUtils.canonicalizeUrl

trait Response {

  def status: Int
  def headers: Map[String, Seq[String]]
  def body: Array[Byte]
  def request: Request
  def meta = request.meta
  def url = request.url
  def follow(
      url: String,
      callback: Response => Seq[Either[Request, Any]] = request.callback,
      method: String = "GET",
      headers: Map[String, String] = request.headers,
      body: Array[Byte] = Array(),
      cookies: Map[String, String] = Map(),
      meta: RequestMeta = request.meta,
      encoding: Charset = request.encoding,
      errback: Exception => Unit = PartialFunction.empty) =
    Request(
      canonicalizeUrl(this.url, url),
      callback,
      method,
      headers,
      body,
      cookies,
      meta,
      encoding,
      errback
    )
}
