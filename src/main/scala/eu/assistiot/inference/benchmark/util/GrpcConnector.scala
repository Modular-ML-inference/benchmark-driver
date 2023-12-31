package eu.assistiot.inference.benchmark.util

import eu.assistiot.inference.benchmark.proto.ExtendedInferenceServiceClient
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.grpc.GrpcClientSettings

class GrpcConnector(host: String, port: Int)(using ActorSystem):
  private val clientSettings = GrpcClientSettings.connectToServiceAt(
    host = host,
    port = port,
  ).withTls(false)

  val client = ExtendedInferenceServiceClient(clientSettings)
