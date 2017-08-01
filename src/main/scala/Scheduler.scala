import Messages.PollRequest
import akka.actor.Actor

import scala.collection.mutable

class Scheduler extends Actor {

  val queue = mutable.Queue[Request]()

  override def receive = {
    case request: Request =>
      queue.enqueue(request)
    case PollRequest =>
      sender() ! queue.dequeue()
  }
}
