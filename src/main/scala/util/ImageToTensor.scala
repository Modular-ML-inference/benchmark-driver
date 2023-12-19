package eu.assistiot.inference.benchmark
package util

import stream.encoding.CarEncodingFlow

import java.awt.image.BufferedImage

object ImageToTensor:
  private val xy = 1200 * 900

  def convert(image: BufferedImage): Seq[Int] =
    val pixels = Array.ofDim[Int](CarEncodingFlow.imageSize)
    for (y <- 0 until 900) do
      for (x <- 0 until 1200) do
        val r = image.getRGB(x, y) >> 16 & 0xFF
        val g = image.getRGB(x, y) >> 8 & 0xFF
        val b = image.getRGB(x, y) & 0xFF
        pixels(y * 1200 + x) = r
        pixels(xy + y * 1200 + x) = g
        pixels(2 * xy + y * 1200 + x) = b

    pixels.toSeq
