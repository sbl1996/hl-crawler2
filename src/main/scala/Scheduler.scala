package haishu.crawler2

import java.util.NoSuchElementException
import java.util.concurrent.{BlockingQueue, LinkedBlockingDeque, TimeUnit, TimeoutException}

import Engine.ReplyRequest
import Messages.PollRequest
import akka.actor.{Actor, ActorRef, Props}

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.Try
import scala.util.control.NonFatal
import scala.concurrent.blocking
import scala.collection.JavaConverters._

object Scheduler {

  case object NoRequest

  def props(engine: ActorRef) = Props(new Scheduler(engine))

}

class Scheduler(engine: ActorRef) extends Actor {

  import Scheduler.NoRequest

  val log = context.system.log

  val queue = new LinkedBlockingDeque[Request]()

  val seen = mutable.Set[Request]()

  def poll() = queue.poll()

  override def receive = {
    case request: Request =>
      if (!seen.contains(request)) {
        log.debug(s"push to queue ${request.url}")
        seen += request
        queue.add(request)
      }
    case PollRequest =>
      val request = poll()
      val engine = sender()
      if (request != null) engine ! ReplyRequest(request)
      else engine ! NoRequest
  }
}
