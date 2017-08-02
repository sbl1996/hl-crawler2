package haishu.crawler2

import Messages.{Download, PollRequest}
import Scheduler.NoRequest
import akka.actor.{Actor, Props}
import okhttp3.OkHttpClient
import scala.concurrent.duration._

object Engine {
  case object Start
  case object AskForRequest
  case class ScheduleRequest(request: Request)
  case class ParseResponse(response: Response)
  case class ProcessItem(item: Item)
  case class ProcessItemNext(item: Item)
  case class ReplyRequest(request: Request)

  sealed trait State
  case object Uninitialized extends State
  case object Working extends State
  case object Waiting extends State

  def props(pipelines: Seq[Pipeline])(implicit client: OkHttpClient) = Props(new Engine(pipelines))

}

class Engine(pipelines: Seq[Pipeline])(implicit client: OkHttpClient) extends Actor {

  import Engine._

  import context.system

  import context.dispatcher

  val log = system.log

  val scheduler = context.actorOf(Scheduler.props, "scheduler")

  val spider = context.actorOf(Spider.props, "spider")

  val downloader = context.actorOf(OkHttpDownloader.props(client), "downloader")

  val itemPipelines = pipelines.map { p =>
    p.onOpen()
    context.actorOf(ItemPipeline.props(p), p.toString)
  }

  val timer = system.scheduler.schedule(200.millis, 200.millis, scheduler, PollRequest)

  var noRequestTimes = 0

  def receive = {
    case ScheduleRequest(request) =>
      scheduler ! request
    case ReplyRequest(request) =>
       downloader ! Download(request)
    case NoRequest =>
      noRequestTimes += 1
      if (noRequestTimes >= 10) timer.cancel()
    case r: Response =>
      spider ! ParseResponse(r)
    case ProcessItem(item) =>
      itemPipelines.headOption.foreach(_ ! ProcessItem(item))
    case ProcessItemNext(item) =>
      val prevIndex = itemPipelines.indexOf(sender())
      itemPipelines(prevIndex + 1) ! ProcessItem(item)
  }

}
