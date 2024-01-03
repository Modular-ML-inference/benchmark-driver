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
  - batchSize – size of each batch
  - intervalMillis – interval between batches in milliseconds
  - nBatches – number of batches to generate
  - host – host to send requests to
  - port – port to send requests to

### Data

TODO

### Benchmark scripts

TODO

### Examples

- Run the `fall` test with 10 workers, 500ms between requests, 100 requests, sending to `10.0.0.2:8080`:
    ```bash
    java -jar /app/benchmark-assembly.jar fall 10 500 100 10.0.0.2 8080
    ```
- Run the `car` test with 5 images per batch, 2 minutes (120000 ms) between batches, 100 batches, sending to `localhost:8080`:
    ```bash
    java -jar /app/benchmark-assembly.jar car 5 120000 100 10.0.0.2 8080
    ```

## Authors

[Piotr Sowiński](https://orcid.org/0000-0002-2543-9461) ([Ostrzyciel](https://github.com/Ostrzyciel))

## License

This project is licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for more information.
