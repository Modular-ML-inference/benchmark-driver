package eu.assistiot.inference.benchmark.stream

import eu.assistiot.inference.benchmark.MetricsCollector
import org.apache.pekko.NotUsed
import org.apache.pekko.stream.scaladsl.*

object BenchmarkFlow:
  private var inFlight = 0

  def requestFlow[In]: Flow[In, (In, Int), NotUsed] =
    Flow[In]
      .zipWithIndex
      .map((in, idx) => {
        val i = (idx % 2_000_000_000).toInt
        inFlight += 1
        MetricsCollector.inFlight.add(inFlight, System.nanoTime())
        MetricsCollector.requestTime.add(i, System.nanoTime())
        (in, i)
      })

  def responseFlow[Out]: Flow[(Out, Int), Out, NotUsed] =
    Flow[(Out, Int)]
      .map((out, idx) => {
        inFlight -= 1
        MetricsCollector.inFlight.add(inFlight, System.nanoTime())
        MetricsCollector.responseTime.add(idx, System.nanoTime())
        out
      })
