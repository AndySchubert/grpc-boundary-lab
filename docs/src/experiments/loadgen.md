# Load Generator

The load generator is a high-performance Java client designed to saturate the system and measure stable state performance.

## Design

- **Thread-Per-Request**: Uses a fixed thread pool to maintain a constant number of concurrent "blocking" clients.
- **Synchronous Stubs**: Simplifies the logic for measuring end-to-end latency from the client's perspective.
- **HdrHistogram**: Uses a High Dynamic Range Histogram to capture latency percentiles (p50, p95, p99, p99.9) with high precision and low overhead.

## Metrics Reported

| Metric | Description |
| :--- | :--- |
| **ok_rps** | Successful requests per second. |
| **p50_us** | Median latency in microseconds. |
| **p99_us** | Tail latency (99th percentile) in microseconds. |
| **errors** | Number of failed requests (e.g., Deadline Exceeded). |

## Usage

The generator is typically run via the root `Makefile`:
```bash
make sweep REQUESTS=50000 CONCURRENCY="1 8 16"
```
