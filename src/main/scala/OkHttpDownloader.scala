package haishu.crawler2

import java.io.IOException
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

import Messages.Download
import akka.actor.{Actor, Props}
import okhttp3.{Call, Callback, Headers, MediaType, OkHttpClient, RequestBody, Request => OkRequest, Response => OkResponse}

import scala.collection.JavaConverters._
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

class OkHttpDownloader(client: OkHttpClient) extends Actor {

  import OkHttpDownloader._
  import context.dispatcher

  val log = context.system.log

  override def receive = {
    case Download(request) =>
      val result = downloadAsync(client, request)
      val engine = sender()
      result onComplete {
        case Success(response) =>
          log.info(s"downloading page success ${request.url}")
          engine ! response
        case Failure(e) =>
          log.warning(s"download page ${request.url} error", e)
          engine ! akka.actor.Status.Failure(e)
      }
  }

}

object OkHttpDownloader {

  def props(client: OkHttpClient) = Props(new OkHttpDownloader(client))

  def downloadAsync(client: OkHttpClient, request: Request): Future[Response] = {
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

  def download(client: OkHttpClient, request: Request): Response = {
    println(s"downloading ${request.url}")
    val okRequest = convertRequest(request)
    val thisClient = buildClient(request.meta, client)
    convertResponse(thisClient.newCall(okRequest).execute(), request)
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
    val charset = Option(okResponse.body().contentType()).flatMap(m => Option(m.charset()))

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
