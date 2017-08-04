import java.util.Date

import akka.actor.{ActorRef, ActorSystem}
import haishu.crawler2.{ConsolePipeline, Engine, Item, Response}
import haishu.crawler2.Engine.ScheduleRequest
import okhttp3.OkHttpClient

object Main {

  implicit val system = ActorSystem("crawler")

  implicit val client = new OkHttpClient()

  def submit(job: Job): Unit = {
    val engine = system.actorOf(Engine.props(job.pipeliens), job.name)
    job.startRequests.foreach(r => engine ! ScheduleRequest(r))
  }

  case class Article(title: String, content: String)

  class ZxfbJob extends SimpleJob {

    val name = "zxfb"

    val startUrls = Seq(
      "http://www.stats.gov.cn/tjsj/zxfb/"
    )

    def parse(r: Response) = {

      val links = r.css(".center_list").links().regex(""".*\d{8}_\d{7}.html$""").all().map(r.follow(_, parseItem))

      send(links)

    }

    def parseItem(r: Response) = {
      val article = for {
        title <- r.css(".xilan_tit", "text").get()
        content <- r.css(".TRS_Editor").get()
      } yield Article(title, content)
      article match {
        case None => println(r.request.url)
        case Some(a) => println(a.title)
      }
      result(article)
    }

  }

  def submit(j: SimpleJob): Unit = submit(j.build())

}
