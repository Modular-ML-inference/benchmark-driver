package eu.assistiot.inference.benchmark.stream

import eu.assistiot.inference.benchmark.MetricsCollector
import eu.assistiot.inference.benchmark.proto.*
import eu.assistiot.inference.benchmark.util.GrpcConnector
import org.apache.pekko.NotUsed
import org.apache.pekko.stream.*
import org.apache.pekko.stream.scaladsl.*

import scala.concurrent.duration.*

object GrpcFlow:
  private var inFlight = 0

  private val restartSettings = RestartSettings(
    minBackoff = 1.second,
    maxBackoff = 15.seconds,
    randomFactor = 0.2,
  )

  def request(s: Source[ExtendedInferenceRequest, NotUsed])(using conn: GrpcConnector):
  Source[ExtendedInferenceResponse, NotUsed] =
    val s2 = s.map(r => {
      inFlight.synchronized {
        inFlight += 1
        val time = System.nanoTime()
        MetricsCollector.inFlight.add(inFlight, time)
        MetricsCollector.requestSentTime.add(r.id, time)
      }
      r
    })

    //RestartSource.withBackoff(restartSettings) { () =>
    conn.client.predict(s2)
      .map(r => {
        inFlight.synchronized {
          inFlight -= 1
          val time = System.nanoTime()
          MetricsCollector.inFlight.add(inFlight, time)
          MetricsCollector.responseReceivedTime.add(r.id, time)
        }
        r
      })
    // }
