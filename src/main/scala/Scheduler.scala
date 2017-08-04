package haishu.crawler2

import java.util.NoSuchElementException
import java.util.concurrent.{BlockingQueue, LinkedBlockingDeque, TimeUnit, TimeoutException}

import Engine.ReplyRequest
import Messages.PollRequest
import akka.actor.{Actor, Props}

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.Try
import scala.util.control.NonFatal
import scala.concurrent.blocking

object Scheduler {

  case object NoRequest

  def props = Props(new Scheduler)

}

class Scheduler extends Actor {

  import Scheduler.NoRequest

  val log = context.system.log

  val queue = new LinkedBlockingDeque[Request]()

  val seen = mutable.Set[Request]()

  def poll = queue.poll(5, TimeUnit.SECONDS)

  override def receive = {
    case request: Request =>
      if (!seen.contains(request)) {
        log.debug(s"push to queue ${request.url}")
        seen += request
        queue.add(request)
      }
    case PollRequest =>
      try {
        val request = queue.poll()
        sender() ! ReplyRequest(request)
      } catch {
        case _: NoSuchElementException =>
          sender() ! NoRequest
      }
  }
}

class StreamScheduler {

  private val queue = new LinkedBlockingDeque[Request]()

  private val seen = mutable.Set[Request]()

  private val defaultTimeout = 10

  private val defaultTimeUnit = TimeUnit.SECONDS

  def poll(timeout: Long = defaultTimeout, unit: TimeUnit = defaultTimeUnit) = {
    val result = queue.poll(timeout, unit)
    if (result == null) throw new TimeoutException(s"Scheduler can not get new request after $timeout $unit")
    else result
  }

  def pollAsync()(implicit ec: ExecutionContext) = Future {
    blocking {
      poll()
    }
  }

  def add(request: Request) = {
    if (!seen.contains(request)) {
      seen += request
      queue.add(request)
    }
  }
}
