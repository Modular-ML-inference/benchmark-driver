package eu.assistiot.inference.benchmark

import stream.encoding.{FallEncodingFlow, FallInferenceInput}
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.*
import stream.source.AccelerationDataSource

import java.nio.file.Path
import scala.concurrent.Await
import scala.concurrent.duration.*

@main
def main(): Unit =
  given ActorSystem = ActorSystem("benchmark")

  val stream = AccelerationDataSource.fromPath(Path.of("data/accel.csv"))
    .delay(1.second, DelayOverflowStrategy.backpressure)
    .sliding(5)
    .zipWithIndex
    .map { case (xs, i) => FallInferenceInput((i % 2_000_000_000).toInt, xs) }
    .via(FallEncodingFlow.encodeTensorFlow)
    .runForeach(println)

  Await.result(stream, 15.seconds)
