package eu.assistiot.inference.benchmark.stream.source

import eu.assistiot.inference.benchmark.util.ImageToTensor
import org.apache.pekko.NotUsed
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.scaladsl.*
import org.apache.pekko.util.ByteString

import java.nio.file.Path
import javax.imageio.ImageIO

object ScratchesDataSource:
  def fromDir(p: Path)(using mat: Materializer): Source[Seq[Int], NotUsed] =
    val images = FileIO.fromPath(p.resolve("car_images.txt"))
      .via(Framing.delimiter(ByteString("\n"), 1024, true).map(_.utf8String))
      .map(p.resolve("car").resolve(_).toFile)
      .map(ImageIO.read)
      .map(ImageToTensor.convert)
      .runWith(Sink.seq)

    Source.future(images)
      .map(s => LazyList.continually(s.to(LazyList)).flatten)
      .mapConcat(identity)
