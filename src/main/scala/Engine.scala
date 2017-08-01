import akka.actor.Actor

class Engine extends Actor {

  def initial: Receive = {
    case Start
  }

  override def receive = {
    case request: Request =>
      scheduler ! request
    case
  }

}

object Engine {

}
