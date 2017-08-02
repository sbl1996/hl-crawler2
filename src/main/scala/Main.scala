import java.util.Date

import akka.actor.{ActorRef, ActorSystem}
import haishu.crawler2.{ConsolePipeline, Engine, Item, Response}
import haishu.crawler2.Engine.ScheduleRequest
import okhttp3.OkHttpClient

object Main extends App {

  implicit val system = ActorSystem("crawler")

  implicit val client = new OkHttpClient()

  def submit(job: Job): Unit = {
    val engine = system.actorOf(Engine.props(job.pipeliens), job.name)
    job.startRequests.foreach(r => engine ! ScheduleRequest(r))
  }

  case class Article(title: String, content: String) extends Item

  class ZxfbJob extends SimpleJob {

    val name = "zxfb"

    val startUrls = Seq(
      "http://www.stats.gov.cn/tjsj/zxfb/"
    )

    override val pipelines = Seq(new ConsolePipeline)

    def parse(r: Response) = {

      val s = r.css(".center_list")

      val links = s.links().all()

      println(links)

      val article = for {
        title <- r.css(".xilan_tit", "text").get()
        content <- r.css(".TRS_Editor").get()
      } yield Article(title, content)

      result(links) ++ result(article)

    }

  }

  def submit(j: SimpleJob): Unit = submit(j.build())

  submit(new ZxfbJob)

}
