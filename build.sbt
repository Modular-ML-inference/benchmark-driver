ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.1"

enablePlugins(PekkoGrpcPlugin)

lazy val pekkoV = "1.0.1"
lazy val pekkoHttpV = "1.0.0"
lazy val pekkoConnV = "1.0.0"

lazy val root = (project in file("."))
  .settings(
    name := "benchmark-driver",
    idePackagePrefix := Some("eu.assistiot.inference.benchmark"),

    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-actor-typed" % pekkoV,
      "org.apache.pekko" %% "pekko-stream-typed" % pekkoV,
      "org.apache.pekko" %% "pekko-http" % pekkoHttpV,
      "org.apache.pekko" %% "pekko-http-core" % pekkoHttpV,
    ),
  )
