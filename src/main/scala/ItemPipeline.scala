package haishu.crawler2

import Engine.{ProcessItem, ProcessItemNext}
import akka.actor.{Actor, Props}

class ItemPipeline(pipeline: Pipeline) extends Actor {

  def receive = {
    case ProcessItem(item) =>
      pipeline.process(item).foreach { result =>
        sender() ! ProcessItemNext(result)
      }
  }

}

object ItemPipeline {

  def props(pipeline: Pipeline) = Props(new ItemPipeline(pipeline))

}