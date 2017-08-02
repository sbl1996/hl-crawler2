package haishu.crawler2

import Engine.{ParseResponse, ProcessItem, ScheduleRequest}
import akka.actor.{Actor, ActorRef, Props}

class Spider extends Actor {

  def receive = {
    case ParseResponse(response) =>
      val results = response.request.callback(response)
      results foreach {
        case Left(request) =>
          sender() ! ScheduleRequest(request)
        case Right(item) =>
          sender() ! ProcessItem(item)
      }
  }
}

object Spider {

  def props = Props(new Spider)

}
