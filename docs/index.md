# gRPC Boundary Lab

This project explores the performance impact of introducing a gateway boundary
in front of a gRPC backend service.

The setup:

- Backend (port 50051)
- Gateway (port 50052 → forwards to backend)
- Load Generator with configurable:
  - concurrency
  - warmup
  - deadlines
  - repeated runs
  - percentile reporting (p50 / p95 / p99)

The goal is to quantify:

- Throughput degradation
- Tail latency amplification
- Saturation behavior under load

---

## Quick Start

### Start backend

```bash
make backend
```

### Start gateway

```bash
make gateway
```

### Run load against backend

```bash
make loadgen-backend
```

### Run full sweep

```bash
make sweep RUNS=5
```
