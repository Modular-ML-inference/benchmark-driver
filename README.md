# benchmark-driver

Scala application posing as the client in benchmarks of the [Modular Inference Server](https://github.com/Modular-ML-inference/inference-server).

## Usage

The easiest way to use this application is to use the provided Docker image:

```bash
docker run -it --rm \
  -v /path/to/data:/worker/data \
  -v /path/to/results:/worker/out \
  ghcr.io/modular-ml-inference/benchmark-driver:main \
  java -jar /app/benchmark-assembly.jar <args>
```

Where `<args>` are the arguments to the benchmark driver:

- test – the test to run (either `fall` or `car`)
- For `fall`:
  - nWorkers – number of workers generating requests
  - intervalMillis – interval between requests in milliseconds
  - nRequests – number of requests to generate
  - host – host to send requests to
  - port – port to send requests to
- For `car`:
  - batchSize – size of batches sent to the MIS
  - intervalMillis – interval between vehicle scans in milliseconds
  - nBatches – number of vehicle scans to generate
  - host – host to send requests to
  - port – port to send requests to

### Data

The inference data should be placed in the `/worker/data` directory of the container. The directory structure should be as follows:

- `/worker/data`
  - `accel.csv` – acceleration data, downloaded from [here](https://github.com/Modular-ML-inference/ml-usecase/blob/main/fall_detection/data/test_accel.csv)
  - `car` – directory containing the subset of the [CarDD dataset](https://cardd-ustc.github.io/), obtained using the instructions [here](https://github.com/Modular-ML-inference/ml-usecase).

### Benchmark scripts

The `bin` directory contains Bash scripts useful for reproducing the experiments from the paper. The scripts use the aforementioned Docker container and assume that the data is placed in the `./data` directory on the host machine, the results are then saved in the `./out` directory.

- `fall.sh` – fall detection benchmark, using only one client at a time (used in the tests with the GWEN).
- `fall_multiple.sh` – fall detection benchmark, using multiple clients at a time (used in the tests with the x86-64 server).
- `car.sh` – scratch detection benchmark.

All scripts take two positional arguments:

- Hostname/IP of the Modular Inference Server
- Port of the Modular Inference Server

### Examples

- Run the `fall` test with 10 workers, 500ms between requests, 100 requests, sending to `10.0.0.2:8080`:
    ```bash
    java -jar /app/benchmark-assembly.jar fall 10 500 100 10.0.0.2 8080
    ```
- Run the `car` test with 4 images per batch, 3 minutes (180000 ms) between scans, 15 vehicle scans, sending to `localhost:8080`:
    ```bash
    java -jar /app/benchmark-assembly.jar car 4 180000 15 10.0.0.2 8080
    ```

## Authors

[Piotr Sowiński](https://orcid.org/0000-0002-2543-9461) ([Ostrzyciel](https://github.com/Ostrzyciel))

## License

This project is licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for more information.
