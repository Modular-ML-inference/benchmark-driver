package eu.assistiot.inference.benchmark.stream

import eu.assistiot.inference.benchmark.MetricsCollector
import org.apache.pekko.NotUsed
import org.apache.pekko.stream.scaladsl.*

import scala.concurrent.duration.FiniteDuration

object BenchmarkFlow:
  def requestPrepareFlow[In](clientNum: Int, initialDelay: FiniteDuration, interval: FiniteDuration, requests: Long):
  Flow[In, (In, Int), NotUsed] =
    val tickSource = Source.tick(initialDelay, interval, ())
    val offset = clientNum * 1_000_000

    Flow[In]
      .take(requests)
      .zipWith(tickSource)((in, _) => in)
      .zipWithIndex
      .map((in, idx) => {
        val i = (idx % 1_000_000).toInt + offset
        MetricsCollector.requestPrepareTime.add(i, System.nanoTime())
        (in, i)
      })

  def responseDecodedFlow[Out]: Flow[(Out, Int), Out, NotUsed] =
    Flow[(Out, Int)]
      .map((out, idx) => {
        MetricsCollector.responseDecodedTime.add(idx, System.nanoTime())
        out
      })
