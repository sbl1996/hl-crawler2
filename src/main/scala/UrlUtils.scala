import java.net.{MalformedURLException, URL}

object UrlUtils {

  def canonicalizeUrl(base: URL, relUrl: String): URL = {
    // workaround: java resolves '//path/file + ?foo' to '//path/?foo', not '//path/file?foo' as desired
    val relUrl2 = if (relUrl.startsWith("?")) base.getPath + relUrl else relUrl
    // workaround: //example.com + ./foo = //example.com/./foo, not //example.com/foo
    val base2 =
      if (relUrl2.indexOf('.') == 0 && base.getFile.indexOf('/') != 0)
        new URL(base.getProtocol, base.getHost, base.getPort, "/" + base.getFile)
      else
        base

    new URL(base2, relUrl2)
  }

  // Borrowed from Jsoup
  def canonicalizeUrl(baseUrl: String, relUrl: String): String = {
    var base: URL = null
    try {
      try
        base = new URL(baseUrl)
      catch {
        case e: MalformedURLException =>
          // the base is unsuitable, but the attribute/rel may be abs on its own, so try that
          val abs = new URL(relUrl)
          return abs.toExternalForm
      }
      encodeIllegalCharacterInUrl(canonicalizeUrl(base, relUrl).toExternalForm)
    } catch {
      case e: MalformedURLException => ""
    }
  }

  def encodeIllegalCharacterInUrl(url: String): String = {
    //TODO more charator support
    url.replace(" ", "%20")
  }

}
