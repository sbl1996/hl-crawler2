import java.util.Date

import akka.actor.{ActorRef, ActorSystem}
import haishu.crawler2._
import haishu.crawler2.Engine.ScheduleRequest
import okhttp3.OkHttpClient

object Main extends App {

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

    override val pipelines = Seq(SingleFilePipeline("/home/hldev/Shen/zxfb"))

    def parse(r: Response) = {

      val links = r.css(".center_list").links().regex(""".*\d{8}_\d{7}.html$""").all().map(r.follow(_, parseItem))

      collectRequests(links)

    }

    def parseItem(r: Response) = {
      val article = for {
        title <- r.css(".xilan_tit", "text").get()
        content <- r.css(".TRS_Editor").get()
      } yield Map("title" -> title, "content" -> content)
      result(article.get)
    }

  }

  def submit(j: SimpleJob): Unit = submit(j.build())

  submit(new ZxfbJob)

}
