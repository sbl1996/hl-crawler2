package haishu.crawler2

import java.net.Proxy

case class RequestMeta(
    redirect: Boolean = true,
    retryTimes: Int = 0,
    mergeCookies: Boolean = true,
    downloadTimeout: Int = 5000,
    proxy: Option[Proxy] = None) {

}
