# benchmark-driver

Scala application posing as the client in benchmarks of the [Modular Inference Server](https://github.com/Modular-ML-inference/inference-server). More benchmark details can be found in ["Flexible Deployment of Machine Learning Inference Pipelines in the Cloud–Edge–IoT Continuum"](https://www.mdpi.com/2079-9292/13/10/1888). 

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
## Citation

If you found the benchmark-driver useful, please consider starring ⭐ us on GitHub and citing 📚 us in your research!

```
Bogacka, K.; Sowiński, P.; Danilenka, A.; Biot, F.M.; Wasielewska-Michniewska, K.; Ganzha, M.; Paprzycki, M.; Palau, C.E.
Flexible Deployment of Machine Learning Inference Pipelines in the Cloud–Edge–IoT Continuum.
Electronics 2024, 13, 1888. https://doi.org/10.3390/electronics13101888 
```

```bibtex
@Article{electronics13101888,
AUTHOR = {Bogacka, Karolina and Sowiński, Piotr and Danilenka, Anastasiya and Biot, Francisco Mahedero and Wasielewska-Michniewska, Katarzyna and Ganzha, Maria and Paprzycki, Marcin and Palau, Carlos E.},
TITLE = {Flexible Deployment of Machine Learning Inference Pipelines in the Cloud–Edge–IoT Continuum},
JOURNAL = {Electronics},
VOLUME = {13},
YEAR = {2024},
NUMBER = {10},
ARTICLE-NUMBER = {1888},
URL = {https://www.mdpi.com/2079-9292/13/10/1888},
ISSN = {2079-9292},
ABSTRACT = {Currently, deploying machine learning workloads in the Cloud–Edge–IoT continuum is challenging due to the wide variety of available hardware platforms, stringent performance requirements, and the heterogeneity of the workloads themselves. To alleviate this, a novel, flexible approach for machine learning inference is introduced, which is suitable for deployment in diverse environments—including edge devices. The proposed solution has a modular design and is compatible with a wide range of user-defined machine learning pipelines. To improve energy efficiency and scalability, a high-performance communication protocol for inference is propounded, along with a scale-out mechanism based on a load balancer. The inference service plugs into the ASSIST-IoT reference architecture, thus taking advantage of its other components. The solution was evaluated in two scenarios closely emulating real-life use cases, with demanding workloads and requirements constituting several different deployment scenarios. The results from the evaluation show that the proposed software meets the high throughput and low latency of inference requirements of the use cases while effectively adapting to the available hardware. The code and documentation, in addition to the data used in the evaluation, were open-sourced to foster adoption of the solution.},
DOI = {10.3390/electronics13101888}
}

```

## Authors

[Piotr Sowiński](https://orcid.org/0000-0002-2543-9461) ([Ostrzyciel](https://github.com/Ostrzyciel))

## License

This project is licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for more information.
