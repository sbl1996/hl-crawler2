package haishu.crawler2

import java.nio.file.{Files, Path, Paths, StandardOpenOption}

import scala.collection.JavaConverters._

class SingleFilePipeline(path: Path) extends Pipeline {

  override def onOpen(): Unit = {
    if (Files.notExists(path)) Files.createFile(path)
  }

  def process(item: Item) = item match {
    case ProductItem(p) =>
      Files.write(path, Seq(p.toString).asJava, StandardOpenOption.APPEND)
      Some(item)
    case MapItem(m) =>
      val lines = m.map { case (k, v) => s"$k: $v" }
      Files.write(path, lines.asJava, StandardOpenOption.APPEND)
      Some(item)
  }

}

object SingleFilePipeline {

  def apply(path: Path): SingleFilePipeline = new SingleFilePipeline(path)

  def apply(path: String): SingleFilePipeline = new SingleFilePipeline(Paths.get(path))

}