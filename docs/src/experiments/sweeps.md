# Load Sweeps

The project includes automation to perform categorical load testing across multiple variables.

## The `sweep` Command

The `Makefile` defines a `sweep` target that iterates through concurrency levels for both the Backend and Gateway.

```bash
make sweep REQUESTS=50000 CONCURRENCY="1 8 16 32 64 128"
```

### Why Sweep?

A single point-in-time benchmark is often misleading. Sweeping across concurrency allows us to:

- **Identify the Knee** — the point where throughput stops growing and latency spikes.
- **Verify Threading Models** — ensure the `Async` gateway isn’t thread-pool constrained.
- **Establish a Comparative Baseline** — compare single-hop vs double-hop gRPC architectures.

## Baseline vs Gateway Sweep Results
Full results are captured in `sweep.txt` in the repository root, serving as the raw data source for the [Scaling Behavior](../analysis/scaling.md)
and [Gateway Overhead](../analysis/gateway-overhead.md) analysis.
