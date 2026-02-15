# Future Work

This project currently focuses on measuring gRPC boundary overhead under controlled load.
Future extensions could include:

## 1. Cross-Machine Benchmarks
Measure gateway + backend on separate nodes to simulate realistic network conditions.

## 2. Protocol Comparisons
Compare:
- gRPC vs REST
- HTTP/1.1 vs HTTP/2
- With and without TLS

## 3. Observability Overhead
Measure the cost of:
- Tracing (OpenTelemetry)
- Metrics collection
- Structured logging

## 4. Autoscaling Experiments
Run load sweeps with Kubernetes HPA enabled and observe scaling dynamics.

## 5. Real-World Payloads
Test:
- Larger protobuf messages
- Streaming RPCs
- Mixed workload profiles