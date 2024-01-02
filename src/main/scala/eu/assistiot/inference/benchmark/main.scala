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
import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, Future}

@main
def main(test: String, arg: Int, intervalMillis: Int, requests: Long, host: String, port: Int): Unit =
  given as: ActorSystem = ActorSystem("benchmark")
  given GrpcConnector = GrpcConnector(host, port)
  given ExecutionContext = as.getDispatcher

  val interval = intervalMillis.milliseconds
  val metricsDir = Path.of(f"out/${System.currentTimeMillis / 1000}")

  MetricsCollector.realTime.add((System.currentTimeMillis, System.nanoTime))

  val metricsFuture = saveMetrics(metricsDir)
  val streamFuture = if test == "car" then runCarTest(interval, requests)
  else if test == "fall" then runFallTest(arg, interval, requests)
  else throw IllegalArgumentException(s"Unknown test: $test")

  metricsFuture.onComplete(_ => {
    println("Metrics saved")
  })
  streamFuture.onComplete(_ => {
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

def runCarTest(interval: FiniteDuration, requests: Long)(using ActorSystem, GrpcConnector): Future[Done] =
  val carStream = ScratchesDataSource.fromDir(Path.of("data"))
    .grouped(3)
    .via(BenchmarkFlow.requestPrepareFlow(0, interval, interval, requests))
    .map { case (xs, i) => CarInferenceRequest(i, xs) }
    .via(CarEncodingFlow.encodeTensorFlow)
    .runWith(Sink.ignore)

  // TODO: gRPC client

  carStream

def runFallTest(nClients: Int, interval: FiniteDuration, requests: Long)(using ActorSystem, GrpcConnector):
Future[Done] =
  val data = AccelerationDataSource(Path.of("data/accel.csv"))

  val sources = for i <- 0 until nClients yield
    val initialDelay = 1.second + (interval / nClients) * i
    val offset = 50 * i

    data.newSource
      .drop(offset)
      .sliding(5)
      .via(BenchmarkFlow.requestPrepareFlow(i, initialDelay, interval, requests))
      .map { case (xs, i) => FallInferenceInput(i, xs) }
      .via(FallEncodingFlow.encodeTensorFlow)
      .buffer(16, OverflowStrategy.backpressure)

  val requestSource = sources.head.mergeAll(sources.tail, false)

  val future = GrpcFlow.request(requestSource)
    .via(FallEncodingFlow.decodeTensorFlow)
    .via(BenchmarkFlow.responseDecodedFlow)
    // .runForeach(println)
    .runWith(Sink.ignore)

  future
