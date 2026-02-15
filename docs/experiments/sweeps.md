# Load Sweeps

The project includes automation to perform categorical load testing across multiple variables.

## The `sweep` Command

The `Makefile` defines a `sweep` target that iterates through concurrency levels for both the Backend and Gateway.

```bash
make sweep REQUESTS=50000 CONCURRENCY="1 8 16 32 64 128"
```

## Why Sweep?

A single point-in-time benchmark is often misleading. By sweeping across concurrency, we can:
1. **Identify the Knee**: The point where throughput stops growing and latency starts spiking.
2. **Verify Threading Models**: Ensure that the `Async` gateway isn't bottlenecked by an insufficiently sized thread pool.
3. **Comparative Baseline**: Directly compare how the same hardware handles a single vs double-hop gRPC architecture.

## Baseline vs Gateway Sweep Results
Full results are captured in `sweep.txt` in the repository root, serving as the raw data source for the [Scaling](./analysis/scaling.md) and [Overhead](./analysis/gateway-overhead.md) analysis.
