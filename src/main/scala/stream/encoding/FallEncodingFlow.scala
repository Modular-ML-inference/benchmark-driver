package eu.assistiot.inference.benchmark
package stream.encoding

import proto.*
import org.apache.pekko.stream.scaladsl.*
import org.tensorflow.framework.*

case class FallInferenceInput(id: Int, windows: Seq[(Int, Int, Int)])

case class FallInferenceOutput(id: Int, confidence: Float)

object FallEncodingFlow extends MlEncodingFlow[FallInferenceInput, FallInferenceOutput]:
  private val multiplier = 8000.0f / 2048.0f

  private val shape = Some(TensorShapeProto(
    dim = Seq(
      TensorShapeProto.Dim(size = 5, name = ""),
      TensorShapeProto.Dim(size = 4, name = ""),
    )
  ))

  override def encodeTensorFlow = Flow[FallInferenceInput]
    .map(input => {
      if input.windows.length != 5 then
        throw new IllegalArgumentException(
          f"Input for fall inference must have 5 windows, has ${input.windows.length}}"
        )
      val flatTensor: Seq[Float] = input.windows.flatMap(window => {
        val (x, y, z) = (window._1 * multiplier, window._2 * multiplier, window._3 * multiplier)
        val mag = Math.sqrt(x * x + y * y + z * z).toFloat
        Seq(x, y, z, mag)
      })
      ExtendedInferenceRequest(
        id = input.id,
        input = Map(
          "acceleration" -> TensorProto(
            dtype = DataType.DT_FLOAT,
            tensorShape = shape,
            floatVal = flatTensor
          )
        )
      )
    })

  override def decodeTensorFlow = Flow[ExtendedInferenceResponse]
    .map(response => FallInferenceOutput(
      id = response.id,
      confidence = response.output("confidence").floatVal.head
    ))
