package eu.assistiot.inference.benchmark
package stream.source

import org.apache.pekko.NotUsed
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.scaladsl.*
import org.apache.pekko.util.ByteString

import java.nio.file.Path
import scala.concurrent.ExecutionContext

object AccelerationDataSource:
  def fromPath(p: Path)(using mat: Materializer): Source[(Int, Int, Int), NotUsed] =
    val data = FileIO.fromPath(p)
      .via(Framing.delimiter(ByteString("\n"), 1024, true).map(_.utf8String))
      .drop(1)
      .map(_.split(",").map(_.toInt))
      .mapConcat(x => Seq(
        (x(0), x(1), x(2)),
        (x(3), x(4), x(5)),
      ))
      .runWith(Sink.seq)

    Source.future(data)
      // Repeat the data forever
      .map(s => LazyList.continually(s.to(LazyList)).flatten)
      .mapConcat(identity)
