package haishu.crawler2

import java.util.NoSuchElementException

import Engine.ReplyRequest
import Messages.PollRequest
import akka.actor.{Actor, Props}

import scala.collection.mutable
import scala.concurrent.duration._
import scala.util.Try
import scala.util.control.NonFatal

object Scheduler {

  case object NoRequest

  def props = Props(new Scheduler)

}

class Scheduler extends Actor {

  import Scheduler.NoRequest

  val log = context.system.log

  val queue = mutable.Queue[Request]()

  val seen = mutable.Set[Request]()

  override def receive = {
    case request: Request =>
      log.debug(s"get a candidate url ${request.url}")
      if (!seen.contains(request)) {
        log.debug(s"push to queue ${request.url}")
        seen += request
        queue.enqueue(request)
      }
    case PollRequest =>
      try {
        val request = queue.dequeue()
        sender() ! ReplyRequest(request)
      } catch {
        case _: NoSuchElementException =>
          sender() ! NoRequest
      }
  }
}
