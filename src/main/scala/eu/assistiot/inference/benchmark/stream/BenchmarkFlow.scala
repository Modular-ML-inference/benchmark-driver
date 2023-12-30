package eu.assistiot.inference.benchmark.stream

import eu.assistiot.inference.benchmark.MetricsCollector
import org.apache.pekko.NotUsed
import org.apache.pekko.stream.DelayOverflowStrategy
import org.apache.pekko.stream.scaladsl.*

import scala.concurrent.duration.FiniteDuration

object BenchmarkFlow:
  private var inFlight = 0

  def requestPrepareFlow[In](interval: FiniteDuration, requests: Long): Flow[In, (In, Int), NotUsed] =
    Flow[In]
      .take(requests)
      .delay(interval, DelayOverflowStrategy.backpressure)
      .zipWithIndex
      .map((in, idx) => {
        val i = (idx % 2_000_000_000).toInt
        // TODO: move inFlight to the next stage, done just when we send the request
        // also add there the actual time of the request
        inFlight += 1
        MetricsCollector.inFlight.add(inFlight, System.nanoTime())
        MetricsCollector.requestPrepareTime.add(i, System.nanoTime())
        (in, i)
      })

  def responseDecodedFlow[Out]: Flow[(Out, Int), Out, NotUsed] =
    Flow[(Out, Int)]
      .map((out, idx) => {
        inFlight -= 1
        MetricsCollector.inFlight.add(inFlight, System.nanoTime())
        MetricsCollector.responseDecodedTime.add(idx, System.nanoTime())
        out
      })
