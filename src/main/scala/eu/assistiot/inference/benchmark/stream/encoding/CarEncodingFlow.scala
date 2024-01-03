package eu.assistiot.inference.benchmark
package stream.encoding

import com.google.protobuf.ByteString
import eu.assistiot.inference.benchmark.util.ImageToTensor
import org.apache.pekko.NotUsed
import proto.*
import org.apache.pekko.stream.scaladsl.*
import org.tensorflow.framework.*

import java.nio.ByteBuffer
import scala.jdk.CollectionConverters.*


case class CarInferenceRequest(id: Int, images: Seq[ByteString])

case class CarInferenceResult(id: Int, nResults: Seq[Long])

object CarEncodingFlow extends MlEncodingFlow[CarInferenceRequest, CarInferenceResult]:
  private val imageSize = ImageToTensor.xy_size * 3

  override def encodeTensorFlow: Flow[CarInferenceRequest, ExtendedInferenceRequest, NotUsed] =
    Flow[CarInferenceRequest].map { request =>
      if request.images.exists(_.size() != imageSize) then
        throw new IllegalArgumentException(s"Image size must be $imageSize")

      ExtendedInferenceRequest(
        id = request.id,
        input = Map(
          "array" -> TensorProto(
            dtype = DataType.DT_UINT8,
            tensorShape = Some(TensorShapeProto(
              dim = Seq(
                TensorShapeProto.Dim(size = request.images.length),
                TensorShapeProto.Dim(size = 900),
                TensorShapeProto.Dim(size = 1200),
                TensorShapeProto.Dim(size = 3),
              )
            )),
            tensorContent = ByteString.copyFrom(request.images.asJava)
          )
        )
      )
    }

  override def decodeTensorFlow: Flow[ExtendedInferenceResponse, (CarInferenceResult, Int), NotUsed] =
    Flow[ExtendedInferenceResponse]
      .map(result => {
        val tensor = result.output("results")
        val nResults = if tensor.int64Val.nonEmpty then tensor.int64Val
        else
          tensor.tensorContent.asScala
            .map(_.byteValue())
            .grouped(8)
            .map(it => ByteBuffer.wrap(it.toArray.reverse).getLong)
            .toSeq
        (
          CarInferenceResult(id = result.id, nResults = nResults),
          result.id
        )
      })
      .wireTap((r, _) => MetricsCollector.scratchesFound.add((r.id, r.nResults)))
