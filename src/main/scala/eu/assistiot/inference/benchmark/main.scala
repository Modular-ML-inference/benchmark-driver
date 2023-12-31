package eu.assistiot.inference.benchmark

import eu.assistiot.inference.benchmark.stream.{BenchmarkFlow, GrpcFlow}
import eu.assistiot.inference.benchmark.util.GrpcConnector
import stream.encoding.*
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.*
import org.apache.pekko.stream.scaladsl.*
import stream.source.{AccelerationDataSource, ScratchesDataSource}

import java.nio.file.Path
import scala.concurrent.Await
import scala.concurrent.duration.*

@main
def main(test: String, intervalMillis: Int, requests: Long, host: String, port: Int): Unit =
  given ActorSystem = ActorSystem("benchmark")

  val interval = intervalMillis.milliseconds

  if test == "car" then runCarTest(interval, requests)
  else if test == "fall" then runFallTest(interval, requests)
  else throw IllegalArgumentException(s"Unknown test: $test")

  given GrpcConnector = GrpcConnector(host, port)

  println("Done")
  MetricsCollector.writeAllToFiles(Path.of(f"out/${System.currentTimeMillis / 1000}"))
  System.exit(0)

def runCarTest(interval: FiniteDuration, requests: Long)(using ActorSystem, GrpcConnector): Unit =
  val carStream = ScratchesDataSource.fromDir(Path.of("data"))
    .grouped(3)
    .via(BenchmarkFlow.requestPrepareFlow(interval, requests))
    .map { case (xs, i) => CarInferenceRequest(i, xs) }
    .via(CarEncodingFlow.encodeTensorFlow)
    .runWith(Sink.ignore)

  // TODO: gRPC client

  Await.result(carStream, Duration.Inf)

def runFallTest(interval: FiniteDuration, requests: Long)(using ActorSystem, GrpcConnector): Unit =
  val requestSource = AccelerationDataSource.fromPath(Path.of("data/accel.csv"))
    .sliding(5)
    .via(BenchmarkFlow.requestPrepareFlow(interval, requests))
    .map { case (xs, i) => FallInferenceInput(i, xs) }
    .via(FallEncodingFlow.encodeTensorFlow)

  val responseStream = GrpcFlow.request(requestSource)
    .via(FallEncodingFlow.decodeTensorFlow)
    .via(BenchmarkFlow.responseDecodedFlow)
    .runForeach(println)
    // .runWith(Sink.ignore)

  Await.result(responseStream, Duration.Inf)
