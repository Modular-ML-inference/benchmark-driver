#!/bin/bash

set -eux

BATCHES="1 4 16"
CLIENTS="1 2 4"

docker pull ghcr.io/modular-ml-inference/benchmark-driver:main

for batch_size in $BATCHES
do
  for client_num in $CLIENTS
  do
    for i in $(seq 1 "$client_num")
    do
      docker run --rm \
        -v ./data:/worker/data \
        -v ./out:/worker/out \
        ghcr.io/modular-ml-inference/benchmark-driver:main \
        java -jar /app/benchmark-assembly.jar car "$batch_size" 180000 15 "$1" "$2" &
    done

    # Wait for all clients and sleep for 30 seconds
    wait
    sleep 30
  done
done
