package eu.assistiot.inference.benchmark

import java.nio.file.{Files, Path}
import scala.collection.mutable

object MetricsCollector:
  // (requestId, time)
  val requestPrepareTime = Metric[(Int, Long)]()
  val requestSentTime = Metric[(Int, Long)]()
  val responseReceivedTime = Metric[(Int, Long)]()
  val responseDecodedTime = Metric[(Int, Long)]()
  // (size, time)
  val inFlight = Metric[(Int, Long)]()
  // (requestId, confidence)
  val fallConfidence = Metric[(Int, Float)]()

  val allMetrics = Seq(
    "requestPrepareTime" -> requestPrepareTime,
    "requestSentTime" -> requestSentTime,
    "responseReceivedTime" -> responseReceivedTime,
    "responseDecodedTime" -> responseDecodedTime,
    "inFlight" -> inFlight,
    "fallConfidence" -> fallConfidence,
  )

  def writeAllToFiles(dir: Path): Unit =
    Files.createDirectories(dir)
    println(s"Writing metrics to $dir")
    allMetrics.foreach { (name, metric) =>
      metric.writeToFile(dir.resolve(s"$name.txt"))
    }

  class Metric[T]:
    private val values = mutable.ArrayBuffer[T]()

    def add(value: T): Unit = values += value

    def get: Seq[T] = values.toSeq

    def writeToFile(p: Path): Unit =
      val writer = java.nio.file.Files.newBufferedWriter(p)
      try
        values.foreach { v =>
          writer.write(v.toString)
          writer.newLine()
        }
      finally writer.close()
