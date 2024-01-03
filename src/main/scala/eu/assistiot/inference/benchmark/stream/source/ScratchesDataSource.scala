package eu.assistiot.inference.benchmark.stream.source

import eu.assistiot.inference.benchmark.util.ImageToTensor
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.scaladsl.*

import java.nio.file.Path
import javax.imageio.ImageIO
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration

class ScratchesDataSource(p: Path)(using mat: Materializer):
  given ExecutionContext = mat.executionContext
  private val dataFuture = Source(p.toFile.listFiles().toSeq)
    .filter(f => f.isFile && f.getName.endsWith(".jpg"))
    .map(ImageIO.read)
    .async
    // parallelize the conversion
    .mapAsync(8)(i => Future {
      ImageToTensor.convert(i)
    })
    .runWith(Sink.seq)

  private val data = Await.result(dataFuture, Duration.Inf)

  def newSource = Source.fromIterator(() => LazyList.continually(data).flatten.iterator)
