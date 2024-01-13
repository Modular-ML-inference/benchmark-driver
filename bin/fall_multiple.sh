#!/bin/bash

set -eux

DEVICES="10 40 160"
CLIENTS="1 4 16"

docker pull ghcr.io/modular-ml-inference/benchmark-driver:main

for device_num in $DEVICES
do
  for client_num in $CLIENTS
  do
    for i in $(seq 1 "$client_num")
    do
      docker run --rm \
        -v ./data:/worker/data \
        -v ./out:/worker/out \
        ghcr.io/modular-ml-inference/benchmark-driver:main \
        java -jar /app/benchmark-assembly.jar fall "$device_num" 500 1800 "$1" "$2" &
    done

    # Wait for all clients and sleep for 30 seconds
    wait
    sleep 30
  done
done
