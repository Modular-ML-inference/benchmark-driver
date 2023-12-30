package eu.assistiot.inference.benchmark
package util

import stream.encoding.CarEncodingFlow

import java.awt.image.BufferedImage

object ImageToTensor:
  private val x_size = 640
  private val y_size = 640
  private val xy = x_size * y_size

  def convert(image: BufferedImage): Seq[Int] =
    val pixels = Array.ofDim[Int](CarEncodingFlow.imageSize)
    for (y <- 0 until y_size) do
      for (x <- 0 until x_size) do
        val r = image.getRGB(x, y) >> 16 & 0xFF
        val g = image.getRGB(x, y) >> 8 & 0xFF
        val b = image.getRGB(x, y) & 0xFF
        pixels(y * x_size + x) = r
        pixels(xy + y * x_size + x) = g
        pixels(2 * xy + y * x_size + x) = b

    pixels.toSeq
