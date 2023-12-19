package eu.assistiot.inference.benchmark
package stream.encoding

import proto.*
import org.apache.pekko.NotUsed
import org.apache.pekko.stream.scaladsl.*

trait MlEncodingFlow[In, Out]:
  def encodeTensorFlow: Flow[In, ExtendedInferenceRequest, NotUsed]

  def decodeTensorFlow: Flow[ExtendedInferenceResponse, Out, NotUsed]
