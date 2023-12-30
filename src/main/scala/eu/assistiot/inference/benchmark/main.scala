package eu.assistiot.inference.benchmark

import stream.encoding.{CarEncodingFlow, CarInferenceRequest, FallEncodingFlow, FallInferenceInput}
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.*
import stream.source.{AccelerationDataSource, ScratchesDataSource}

import java.nio.file.Path
import scala.concurrent.Await
import scala.concurrent.duration.*

@main
def main(): Unit =
  given ActorSystem = ActorSystem("benchmark")

  val accelStream = AccelerationDataSource.fromPath(Path.of("data/accel.csv"))
    .delay(1.second, DelayOverflowStrategy.backpressure)
    .sliding(5)
    .zipWithIndex
    .map { case (xs, i) => FallInferenceInput((i % 2_000_000_000).toInt, xs) }
    .via(FallEncodingFlow.encodeTensorFlow)
//    .runForeach(println)
//
//  Await.result(stream, 15.seconds)

  val carStream = ScratchesDataSource.fromDir(Path.of("data"))
    .delay(4.second, DelayOverflowStrategy.backpressure)
    .sliding(3)
    .zipWithIndex
    .map { case (xs, i) => CarInferenceRequest((i % 2_000_000_000).toInt, xs) }
    .via(CarEncodingFlow.encodeTensorFlow)
    .runForeach(println)

  Await.result(carStream, 15.seconds)


