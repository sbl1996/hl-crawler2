import java.io.IOException
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

import Messages.Download
import akka.actor.Actor
import okhttp3.{Call, Callback, Headers, MediaType, OkHttpClient, RequestBody, Request => OkRequest, Response => OkResponse}

import scala.collection.JavaConverters._
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

class OkHttpDownloader(client: OkHttpClient) extends Actor {

  import OkHttpDownloader._

  override def receive = {
    case Download(request) =>
      val result = download(client, request)
      result onComplete {
        case Success(response) =>
          sender() ! response
        case Failure(e) =>
          sender() ! akka.actor.Status.Failure(e)
      }
  }

}

object OkHttpDownloader {

  def download(client: OkHttpClient, request: Request): Future[Response] = {
    val okRequest = convertRequest(request)
    val thisClient = buildClient(request.meta, client)
    val promise = Promise[Response]()
    thisClient.newCall(okRequest).enqueue(new Callback {

      override def onFailure(call: Call, e: IOException) = promise.failure(e)

      override def onResponse(call: Call, okResponse: OkResponse) = {
        val response = convertResponse(okResponse, request)
        promise.success(response)
      }
    })
    promise.future
  }

  def buildClient(meta: RequestMeta, client: OkHttpClient) = {
    client.newBuilder()
      .connectTimeout(meta.downloadTimeout, TimeUnit.MILLISECONDS)
      .followRedirects(meta.redirect)
      .proxy(meta.proxy.orNull)
      .build()
  }

  def convertRequest(request: Request): OkRequest = {

    val okRequestBody = {
      val body = request.body
      if (body.isEmpty) null
      else RequestBody.create(null, body)
    }

    new OkRequest.Builder()
      .url(request.url)
      .method(request.method, okRequestBody)
      .headers(Headers.of(request.headers.asJava))
      .build()
  }

  def convertResponse(okResponse: OkResponse, request: Request): Response = {
    val charset = Option(okResponse.body().contentType()).map(_.charset())

    var headers = Map[String, Seq[String]]()
    for (n <- okResponse.headers().names().asScala) {
      headers += (n -> okResponse.headers(n).asScala)
    }
    HtmlResponse(
      okResponse.code(),
      headers,
      okResponse.body().bytes(),
      request,
      charset
    )
  }

}
