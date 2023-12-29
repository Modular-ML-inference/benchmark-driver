package eu.assistiot.inference.benchmark
package stream.encoding

import proto.*
import org.apache.pekko.stream.scaladsl.*
import org.tensorflow.framework.*

// 3 * 900 * 1200 = 3 240 000 in length
case class CarInferenceRequest(id: Int, images: Seq[Seq[Int]])

case class CarInferenceResult(
  id: Int,
  results: Seq[Int],
  boxes: Seq[Float],
  labels: Seq[Long],
  scores: Seq[Float],
  masks: Seq[Int],
)

object CarEncodingFlow extends MlEncodingFlow[CarInferenceRequest, CarInferenceResult]:
  val imageSize = 3 * 900 * 1200

  override def encodeTensorFlow = Flow[CarInferenceRequest].map { request =>
    if request.images.exists(_.length != imageSize) then
      throw new IllegalArgumentException(s"Image size must be $imageSize")

    ExtendedInferenceRequest(
      id = request.id,
      input = Map(
        "image" -> TensorProto(
          dtype = DataType.DT_UINT8,
          tensorShape = Some(TensorShapeProto(
            dim = Seq(
              TensorShapeProto.Dim(size = request.images.length),
              TensorShapeProto.Dim(size = 3),
              TensorShapeProto.Dim(size = 900),
              TensorShapeProto.Dim(size = 1200),
            )
          )),
          intVal = request.images.flatten,
        )
      )
    )
  }

  override def decodeTensorFlow = Flow[ExtendedInferenceResponse].map { result =>
    CarInferenceResult(
      id = result.id,
      results = result.output("results").intVal,
      boxes = result.output("boxes").floatVal,
      labels = result.output("labels").int64Val,
      scores = result.output("scores").floatVal,
      masks = result.output("masks").intVal,
    )
  }
