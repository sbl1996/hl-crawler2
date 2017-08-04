import java.awt.print.PrinterIOException
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicReference

import akka.stream.OverflowStrategy

object TryStream extends App {

  import akka.NotUsed
  import akka.actor.ActorSystem
  import akka.stream.{ActorAttributes, ActorMaterializer, ClosedShape}
  import akka.stream.scaladsl._
  import haishu.crawler2._
  import okhttp3.OkHttpClient

  import scala.collection.immutable

  implicit val system = ActorSystem()

  implicit val materializer = ActorMaterializer()

  val client = new OkHttpClient()

  class RequestChecker {
    val seen = collection.mutable.Set[Request]()

    def check(r: Request) =
      if (seen contains r) None
      else {
        seen += r
        Some(r)
      }

  }

  def streamJob(job: Job) = {
    val f1 = Flow[Request].map(OkHttpDownloader.download(client, _)).named("download")

    val f2 = Flow[Response].map(r => r.request.callback(r)).named("parse")

    type ParseResult = immutable.Seq[Either[Request, Item]]

    val filterRequest = Flow[ParseResult].mapConcat[Request] {
      result => result.collect {
        case Left(request) => request
      }
    }.named("filterRequest")

    val filterItem = Flow[ParseResult].mapConcat[Item] {
      result => result.collect {
        case Right(item) => item
      }
    }.named("filterItem")

    val scheduler = new StreamScheduler

    val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._
      val start = Source(job.startRequests.toList)
      val mergeRequests = builder.add(MergePreferred[Request](1))
      val bcast = builder.add(Broadcast[ParseResult](2))
      val sink1 = Sink.foreach(scheduler.add)
      val source = Source.fromIterator { () =>
        new Iterator[Request] {
          val prev = new AtomicReference[Request]()
          override def hasNext = {
            try {
              prev.set(scheduler.poll())
              true
            } catch {
              case _: TimeoutException =>
                println("asdasd")
                false
            }
          }

          override def next() = prev.get
        }
      }
        .withAttributes(ActorAttributes.dispatcher(""))

      def pipelineToFlow(pipeline: Pipeline) = {
        val f = Flow[Item].map(pipeline.process)
        val collect = Flow[Option[Item]].collect {
          case Some(item) => item
        }
        f.via(collect)
      }

      val pipelineFlows =
        if (job.pipeliens.isEmpty) Flow[Item].map(identity)
        else job.pipeliens.map(pipelineToFlow).reduce(_ via _)

      start ~> mergeRequests ~> sink1
               mergeRequests.preferred <~ filterRequest <~ bcast
                                                 bcast ~> filterItem ~> pipelineFlows ~> Sink.ignore
                           source ~> f1 ~> f2 ~> bcast
      ClosedShape
    })

    g.run()

  }

  def streamJob2(job: Job) = {
    val downloadFlow = Flow[Request].map(OkHttpDownloader.download(client, _)).named("download")

    val parseFlow = Flow[Response].map { r =>
      r.request.callback(r)
    }.named("parse")

    type ParseResult = immutable.Seq[Either[Request, Item]]

    val filterRequest = Flow[ParseResult].mapConcat[Request] { result =>
      result.collect {
        case Left(request) =>
          println(request)
          request
      }
    }.named("filterRequest")

    val filterItem = Flow[ParseResult].mapConcat[Item] {
      result => result.collect {
        case Right(item) => item
      }
    }.named("filterItem")

    val checker = new RequestChecker

    val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._
      val start = Source(job.startRequests.toList)
      val concat = builder.add(Concat[Request](2))
      val bcast = builder.add(Broadcast[ParseResult](2))
      val checkFlow = Flow[Request].map(checker.check).collect {
        case Some(r) => r
      }

      def pipelineToFlow(pipeline: Pipeline) = {
        Flow[Item].map(pipeline.process).collect {
          case Some(item) => item
        }
      }

      val pipelineFlows =
        if (job.pipeliens.isEmpty) Flow[Item].map(identity)
        else job.pipeliens.map(pipelineToFlow).reduce(_ via _)

      start ~> concat ~> checkFlow ~> downloadFlow ~> parseFlow ~> bcast ~> filterItem ~> pipelineFlows ~> Sink.ignore
               concat <~ Flow[Request].buffer(20, OverflowStrategy.dropHead) <~ filterRequest <~ bcast
      ClosedShape
    })

    g.run()

  }

  def submit(job: SimpleJob) = {
    streamJob2(job.build())
  }

  submit(new Main.ZxfbJob)
  //(1 to 20).foreach(_ => submit(new Main.ZxfbJob))



}