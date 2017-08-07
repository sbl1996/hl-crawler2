package haishu.crawler2

import Messages.{Download, PollRequest}
import Scheduler.NoRequest
import akka.actor.{Actor, Cancellable, Props}
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

  val scheduler = context.actorOf(Scheduler.props(self), "scheduler")

  val spider = context.actorOf(Spider.props, "spider")

  val downloader = context.actorOf(OkHttpDownloader.props(client), "downloader")

  val itemPipelines = pipelines.map { p =>
    p.onOpen()
    context.actorOf(ItemPipeline.props(p), p.toString)
  }

  var timer: Cancellable = _

  var noRequestTimes = 0

  override def preStart() = {
    timer = system.scheduler.schedule(200.millis, 200.millis, scheduler, PollRequest)
  }

  override def postStop() = {
    timer.cancel()
    log.debug(s"Job ${self.path.name} complete")
  }

  def receive = {
    case ScheduleRequest(request) =>
      scheduler ! request

    case ReplyRequest(request) =>
      noRequestTimes = 0
       downloader ! Download(request)
    case NoRequest =>
      noRequestTimes += 1
      if (noRequestTimes >= 10) context.stop(self)

    case r: Response =>
      spider ! ParseResponse(r)

    case ProcessItem(item) =>
      itemPipelines.headOption.foreach(_ ! ProcessItem(item))
    case ProcessItemNext(item) =>
      val nextIndex = itemPipelines.indexOf(sender()) + 1
      if (nextIndex < pipelines.length) itemPipelines(nextIndex) ! ProcessItem(item)
  }

}
