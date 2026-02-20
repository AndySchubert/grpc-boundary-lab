# System Overview

The system consists of three components:

Backend
- Port: 50051
- Implements PingService
- Handles unary gRPC requests

Gateway
- Port: 50052
- Accepts the same gRPC API
- Forwards requests to backend
- Introduces an additional network + serialization boundary

Load Generator
- Blocking gRPC client
- Uses a fixed thread pool
- Supports configurable concurrency
- Reports latency percentiles via HdrHistogram
