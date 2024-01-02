package eu.assistiot.inference.benchmark
package stream.source

import org.apache.pekko.NotUsed
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.scaladsl.*
import org.apache.pekko.util.ByteString

import java.nio.file.Path
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class AccelerationDataSource(p: Path)(using mat: Materializer):
  private val dataFuture = FileIO.fromPath(p)
    .via(Framing.delimiter(ByteString("\n"), 1024, true).map(_.utf8String))
    .drop(1)
    .map(_.split(",").map(_.toInt))
    .mapConcat(x => Seq(
      (x(0), x(1), x(2)),
      (x(3), x(4), x(5)),
    ))
    .runWith(Sink.seq)
  
  private val data = LazyList.continually(
    Await.result(dataFuture, Duration.Inf)
  ).flatten
  
  def newSource: Source[(Int, Int, Int), NotUsed] =
    Source.fromIterator(() => data.iterator)
