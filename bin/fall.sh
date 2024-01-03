#!/bin/bash

set -eux

DEVICES="10 20 40 80 160"

docker pull ghcr.io/modular-ml-inference/benchmark-driver:main

for device_num in $DEVICES
do
  docker run -it --rm \
    -v ./data:/worker/data \
    -v ./results:/worker/out \
    ghcr.io/modular-ml-inference/benchmark-driver:main \
    java -jar /app/benchmark-assembly.jar fall "$device_num" 500 1800 "$1" "$2"

  # Sleep for 30 seconds
  sleep 30
done
