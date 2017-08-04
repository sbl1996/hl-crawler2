import akka.actor.ActorRef
import com.sun.glass.ui.MenuItem.Callback
import haishu.crawler2.Engine.ScheduleRequest
import haishu.crawler2._
import haishu.crawler2.selector.Selectable
import collection.immutable

class Job(
    val name: String,
    val startRequests: Seq[Request],
    val pipeliens: Seq[Pipeline])

trait SimpleJob {

  type ParseResult = immutable.Seq[Either[Request, Item]]

  def result(urls: Seq[String]): ParseResult =
    urls.map(url => Left(Request(url, parse))).toList

  def result(urls: Seq[String], callback: Response => ParseResult): ParseResult =
    urls.map(url => Left(Request(url, callback))).toList

  def send(requests: Seq[Request]): ParseResult =
    requests.map(Left(_)).toList

  def result(item: Item): ParseResult = List(Right(item))

  def result(p: Product): ParseResult = List(Right(ProductItem(p)))

  def result(p: Option[Product]): ParseResult =
    if (p.isEmpty) List() else result(p.get)

  def name: String

  def startUrls: Seq[String]

  def startRequests: Seq[Request] = Seq()

  def parse(response: Response): immutable.Seq[Either[Request, Item]]

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
