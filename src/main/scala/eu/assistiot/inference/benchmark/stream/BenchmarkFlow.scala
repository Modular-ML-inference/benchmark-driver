package eu.assistiot.inference.benchmark.stream

import eu.assistiot.inference.benchmark.MetricsCollector
import org.apache.pekko.NotUsed
import org.apache.pekko.stream.scaladsl.*

object BenchmarkFlow:
  def requestPrepareFlow[In](clientNum: Int):
  Flow[In, (In, Int), NotUsed] =
    val offset = clientNum * 1_000_000

    Flow[In]
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
