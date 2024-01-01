package eu.assistiot.inference.benchmark
package stream.encoding

import org.apache.pekko.NotUsed
import proto.*
import org.apache.pekko.stream.scaladsl.*
import org.tensorflow.framework.*

case class FallInferenceInput(id: Int, windows: Seq[(Int, Int, Int)])

case class FallInferenceOutput(id: Int, confidence: Float)

object FallEncodingFlow extends MlEncodingFlow[FallInferenceInput, FallInferenceOutput]:
  private val shape = Some(TensorShapeProto(
    dim = Seq(
      TensorShapeProto.Dim(size = 5),
      TensorShapeProto.Dim(size = 3),
    )
  ))

  override def encodeTensorFlow: Flow[FallInferenceInput, ExtendedInferenceRequest, NotUsed] =
    Flow[FallInferenceInput]
      .map(input => {
        if input.windows.length != 5 then
          throw new IllegalArgumentException(
            f"Input for fall inference must have 5 windows, has ${input.windows.length}}"
          )
        val flatTensor: Seq[Int] = input.windows.flatMap(w => Seq(w._1, w._2, w._3))
        ExtendedInferenceRequest(
          id = input.id,
          input = Map(
            "array" -> TensorProto(
              dtype = DataType.DT_INT16,
              tensorShape = shape,
              intVal = flatTensor,
            )
          )
        )
      })

  override def decodeTensorFlow: Flow[ExtendedInferenceResponse, (FallInferenceOutput, Int), NotUsed] =
    Flow[ExtendedInferenceResponse]
      .map(response => (
        FallInferenceOutput(
          id = response.id,
          confidence = response.output("prediction").floatVal.head
        ),
        response.id
      ))
      .wireTap((o, _) => MetricsCollector.fallConfidence.add((o.id, o.confidence)))
