# grpc-boundary-lab

Exploring the performance impact of introducing a gRPC gateway boundary in front of a backend service.

This project measures:

- Throughput degradation
- Tail latency amplification (p95 / p99)
- Saturation behavior under load

The system consists of:

- Backend (port 50051)
- Gateway (port 50052 → forwards to backend)
- Load Generator with configurable concurrency, warmup, deadlines, and multi-run sweeps

---

## Architecture

Client → Backend

Client → Gateway → Backend

The gateway introduces an additional network hop, serialization cycle, and scheduling layer.  
This lab quantifies the overhead.

---

## Quick Start

Start backend:

```bash
make backend
```

Start gateway:

```bash
make gateway
```

Run load against backend:

```bash
make loadgen-backend
```

Run full sweep:

```bash
make sweep RUNS=5
```

---

## Documentation

Full documentation (architecture, experiments, analysis) is available via MkDocs:

```bash
make docs
```

---

## Goals

This lab aims to provide a reproducible framework for analyzing:

- Boundary overhead in microservice architectures
- Forwarding bottlenecks
- Tail latency amplification under concurrency
- Saturation characteristics

---

## Status

Current version includes:

- Concurrency-controlled load generator
- Warmup phase
- Deadline configuration
- Percentile latency reporting (HdrHistogram)
- Multi-run stability mode
- Automated sweep target
- MkDocs documentation with Mermaid diagrams
