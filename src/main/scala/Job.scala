import akka.actor.ActorRef
import com.sun.glass.ui.MenuItem.Callback
import haishu.crawler2.Engine.ScheduleRequest
import haishu.crawler2.{Item, Pipeline, Request, Response}

class Job(
    val name: String,
    val startRequests: Seq[Request],
    val pipeliens: Seq[Pipeline])

trait SimpleJob {

  type ParseResult = Seq[Either[Request, Item]]

  def result(urls: Seq[String]): ParseResult =
    urls.map(url => Left(Request(url, parse)))

  def result(urls: Seq[String], callback: Response => ParseResult): ParseResult =
    urls.map(url => Left(Request(url, callback)))

  def result(item: Item): ParseResult = Seq(Right(item))

  def result(item: Option[Item]): ParseResult =
    if (item.isEmpty) Seq() else result(item.get)

  def name: String

  def startUrls: Seq[String]

  def startRequests: Seq[Request] = Seq()

  def parse(response: Response): Seq[Either[Request, Item]]

  def pipelines: Seq[Pipeline] = Seq()

  def build() = {
    val requests = startUrls.map { url =>
      Request(url, parse)
    }
    new Job(
      name,
      startRequests ++ requests,
      pipelines
    )
  }

}
