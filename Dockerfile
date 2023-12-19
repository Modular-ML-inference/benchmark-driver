# GraalVM CE 22.3.3-b1 Java 17, Scala 3.3.1, SBT 1.9.8
FROM sbtscala/scala-sbt:graalvm-ce-22.3.3-b1-java17_1.9.8_3.3.1 as builder

# Copy the project sources
COPY . /app

# Build the project
WORKDIR /app
RUN sbt assembly

# Create the final image
FROM eclipse-temurin:21-jre-jammy
MAINTAINER "Piotr Sowiński <piotr.sowinski@ibspan.waw.pl>"

# Copy the executable jar
COPY --from=builder /app/target/assembly/benchmark-assembly.jar /app/
COPY bin/benchmark /usr/local/bin/benchmark
RUN chmod +x /usr/local/bin/benchmark

WORKDIR /worker
ENTRYPOINT []
