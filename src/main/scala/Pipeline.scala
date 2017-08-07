package haishu.crawler2

import java.nio.file._

import scala.collection.JavaConverters._

trait Pipeline {

  def process(item: Item): Option[Item]

  def onOpen(): Unit = ()

  def onClose(): Unit = ()
}

class ConsolePipeline extends Pipeline {

  def process(item: Item) = {
    println(item.toString)
    Some(item)
  }

}