package haishu.crawler2

import java.nio.file.{Files, Path, Paths}
import java.util.concurrent.Executors

import Engine.ProcessItem
import akka.actor.Actor

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

class SepratedFilePipeline(path: Path) extends {

  def onOpen(): Unit = {
    if (Files.notExists(path)) Files.createDirectory(path)
  }

  var i = 0

  def process(item: Item) = item match {
    case ProductItem(p) =>

  }

}