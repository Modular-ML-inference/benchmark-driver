package eu.assistiot.inference.benchmark

import eu.assistiot.inference.benchmark.stream.encoding.*
import eu.assistiot.inference.benchmark.stream.source.{AccelerationDataSource, ScratchesDataSource}
import eu.assistiot.inference.benchmark.stream.{BenchmarkFlow, GrpcFlow}
import eu.assistiot.inference.benchmark.util.GrpcConnector
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.*
import org.apache.pekko.stream.scaladsl.*

import java.nio.file.Path
import scala.concurrent.duration.*
import scala.concurrent.{Await, ExecutionContext}

@main
def main(test: String, nClients: Int, intervalMillis: Int, requests: Long, host: String, port: Int): Unit =
  given ActorSystem = ActorSystem("benchmark")

  val interval = intervalMillis.milliseconds

  if test == "car" then runCarTest(interval, requests)
  else if test == "fall" then runFallTest(nClients, interval, requests)
  else throw IllegalArgumentException(s"Unknown test: $test")

  given GrpcConnector = GrpcConnector(host, port)

  println("Done")
  MetricsCollector.writeAllToFiles(Path.of(f"out/${System.currentTimeMillis / 1000}"))
  System.exit(0)

def runCarTest(interval: FiniteDuration, requests: Long)(using ActorSystem, GrpcConnector): Unit =
  val carStream = ScratchesDataSource.fromDir(Path.of("data"))
    .grouped(3)
    .via(BenchmarkFlow.requestPrepareFlow(0, interval, interval, requests))
    .map { case (xs, i) => CarInferenceRequest(i, xs) }
    .via(CarEncodingFlow.encodeTensorFlow)
    .runWith(Sink.ignore)

  // TODO: gRPC client

  Await.result(carStream, Duration.Inf)

def runFallTest(nClients: Int, interval: FiniteDuration, requests: Long)(using as: ActorSystem, c: GrpcConnector):
Unit =
  given ExecutionContext = as.getDispatcher
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
    .runForeach(println)
    // .runWith(Sink.ignore)

  Await.result(future, Duration.Inf)
  println("All streams finished")
