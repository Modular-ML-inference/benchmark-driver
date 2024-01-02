package eu.assistiot.inference.benchmark

import java.nio.file.{Files, Path, StandardOpenOption}
import scala.collection.mutable
import scala.jdk.CollectionConverters.*

object MetricsCollector:
  // (wall clock time, nano time)
  val realTime = Metric[(Long, Long)]()
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
    "realTime" -> realTime,
    "requestPrepareTime" -> requestPrepareTime,
    "requestSentTime" -> requestSentTime,
    "responseReceivedTime" -> responseReceivedTime,
    "responseDecodedTime" -> responseDecodedTime,
    "inFlight" -> inFlight,
    "fallConfidence" -> fallConfidence,
  )

  def writeAllToFiles(dir: Path): Unit =
    allMetrics.foreach { (name, metric) =>
      metric.writeToFile(dir.resolve(s"$name.txt"))
    }

  class Metric[T]:
    private val values = mutable.ArrayBuffer[T]()

    def add(value: T): Unit = values.synchronized {
      values += value
    }

    def writeToFile(p: Path): Unit =
      val toSave = values.synchronized {
        if values.isEmpty then None
        else
          val copy = values.toSeq
          values.clear()
          Some(copy)
      }
      toSave match
        case None => ()
        case Some(values) =>
          val writer = java.nio.file.Files.newBufferedWriter(p, StandardOpenOption.CREATE, StandardOpenOption.APPEND)
          try
            values.foreach { v =>
              writer.write(v.toString)
              writer.newLine()
            }
          finally writer.close()
