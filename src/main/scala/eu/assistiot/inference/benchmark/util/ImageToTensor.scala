package eu.assistiot.inference.benchmark
package util

import com.google.protobuf.ByteString
import stream.encoding.CarEncodingFlow

import java.awt.image.BufferedImage

object ImageToTensor:
  private val x_size = 1200
  private val y_size = 900
  val xy_size = x_size * y_size

  def convert(image: BufferedImage): ByteString =
    val pixels = Array.ofDim[Byte](3 * xy_size)
    for (y <- 0 until y_size) do
      for (x <- 0 until x_size) do
        val start = 3 * (y * x_size + x)
        val rgb = image.getRGB(x, y)
        pixels(start) = (rgb >> 16 & 0xFF).toByte
        pixels(start + 1) = (rgb >> 8 & 0xFF).toByte
        pixels(start + 2) = (rgb & 0xFF).toByte

    ByteString.copyFrom(pixels)
