package eu.assistiot.inference.benchmark

import eu.assistiot.inference.benchmark.stream.encoding.*
import eu.assistiot.inference.benchmark.stream.source.{AccelerationDataSource, ScratchesDataSource}
import eu.assistiot.inference.benchmark.stream.{BenchmarkFlow, GrpcFlow}
import eu.assistiot.inference.benchmark.util.GrpcConnector
import org.apache.pekko.Done
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.*
import org.apache.pekko.stream.scaladsl.*

import java.nio.file.{Files, Path}
import java.util.UUID
import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, Future}

@main
def main(test: String, arg: Int, intervalMillis: Int, requests: Long, host: String, port: Int): Unit =
  given as: ActorSystem = ActorSystem("benchmark")
  given GrpcConnector = GrpcConnector(host, port)
  given ExecutionContext = as.getDispatcher

  val interval = intervalMillis.milliseconds
  val metricsDir = Path.of(f"out/${System.currentTimeMillis / 1000}_${UUID.randomUUID.toString.substring(0, 8)}")

  MetricsCollector.realTime.add((System.currentTimeMillis, System.nanoTime))

  val metricsFuture = saveMetrics(metricsDir)
  val streamFuture = if test == "car" then runCarTest(arg, interval, requests)
  else if test == "fall" then runFallTest(arg, interval, requests)
  else throw IllegalArgumentException(s"Unknown test: $test")

  metricsFuture.onComplete(_ => {
    println("Metrics saved")
  })
  streamFuture
    .recover(e => {
      println(s"Stream failed: $e")
      e.printStackTrace()
    })
    .onComplete(_ => {
      MetricsCollector.writeAllToFiles(metricsDir)
      println("Done")
      System.exit(0)
    })

def saveMetrics(dir: Path)(using ActorSystem): Future[Done] =
  Files.createDirectories(dir)
  Source.tick(5.seconds, 2.seconds, ())
    .runForeach(_ => {
      MetricsCollector.writeAllToFiles(dir)
    })

def runCarTest(batchSize: Int, interval: FiniteDuration, requests: Long)(using ActorSystem, GrpcConnector):
Future[Done] =
  println("Loading data...")
  val data = ScratchesDataSource(Path.of("data/car"))
  println("Data loaded")

  val tickSource = Source.tick(interval, interval, ())

  val requestSource = data.newSource
    .grouped(100)
    .take(requests)
    .zipWith(tickSource)((in, _) => in)
    .mapConcat(_.grouped(batchSize).toSeq)
    .via(BenchmarkFlow.requestPrepareFlow(0))
    .map { case (xs, i) => CarInferenceRequest(i, xs) }
    .via(CarEncodingFlow.encodeTensorFlow)
    .buffer(4, OverflowStrategy.backpressure)
    .wireTap(r => println(s"Sending request ${r.id}"))

  GrpcFlow.request(requestSource)
    .via(CarEncodingFlow.decodeTensorFlow)
    .via(BenchmarkFlow.responseDecodedFlow)
    .wireTap(println(_))
    .runWith(Sink.ignore)

def runFallTest(nClients: Int, interval: FiniteDuration, requests: Long)(using ActorSystem, GrpcConnector):
Future[Done] =
  val data = AccelerationDataSource(Path.of("data/accel.csv"))
  val sources = for i <- 0 until nClients yield
    val initialDelay = 1.second + (interval / nClients) * i
    val offset = 50 * i
    val tickSource = Source.tick(initialDelay, interval, ())

    data.newSource
      .drop(offset)
      .sliding(9)
      .take(requests)
      .zipWith(tickSource)((in, _) => in)
      .via(BenchmarkFlow.requestPrepareFlow(i))
      .map { case (xs, i) => FallInferenceInput(i, xs) }
      // .wireTap(println(_))
      .via(FallEncodingFlow.encodeTensorFlow)
      .buffer(16, OverflowStrategy.backpressure)

  val requestSource = sources.head.mergeAll(sources.tail, false)

  val future = GrpcFlow.request(requestSource)
    .via(FallEncodingFlow.decodeTensorFlow)
    .via(BenchmarkFlow.responseDecodedFlow)
    // .wireTap(println(_))
    .runWith(Sink.ignore)

  future
