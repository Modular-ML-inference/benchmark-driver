package eu.assistiot.inference.benchmark.util

object Util:
  def bytesToHex(bytes: Array[Byte]) =
    val result = new StringBuilder
    for (aByte <- bytes) {
      result.append(String.format("%02x", aByte))
    }
    result.toString
